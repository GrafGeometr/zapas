//! Модуль с сервисом доставки сообщений для SSE.
use serde::{Deserialize, Serialize};
use std::{
    collections::HashMap,
    sync::{Arc, Mutex},
};
use tokio::sync::broadcast;
use tracing::error;
use utoipa::ToSchema;
use uuid::Uuid;

/// Тип событий, которые могут приходить из SSE.
#[derive(Clone, Copy, Serialize, Deserialize, ToSchema)]
pub enum EventType {
    Session,
    User,

    Member,

    Room,
    Storage,
    Item,
}

/// Событие, о котором сообщается через SSE.
#[derive(Clone, Copy, Serialize, Deserialize, ToSchema)]
pub struct EventMsg {
    pub event_type: EventType,

    pub target_id: Uuid,
    pub room_id: Option<Uuid>,
    pub user_id: Option<Uuid>,
}

/// Сервис по доставке SSE сообщений внутри приложения.
#[derive(Clone, Default)]
pub struct SseService {
    channels: Arc<Mutex<HashMap<Uuid, broadcast::Sender<EventMsg>>>>,
}

/// RAII обертка для автоматической очистки неиспользуемых ресурсов в `SseService`.
pub struct ReceiverGuard {
    sse: SseService,
    user: Uuid,

    receiver: Option<broadcast::Receiver<EventMsg>>,
}

impl SseService {
    /// Создание сервиса.
    pub fn new() -> Self {
        Self::default()
    }

    /// Отправка события.
    pub fn send(&self, user: Uuid, msg: EventMsg) {
        let map = self.channels.lock().expect("Map lock");

        if let Some(tx) = map.get(&user) {
            if let Err(err) = tx.send(msg) {
                error!("Sse service send error: {}", err);
            }
        }
    }

    /// Получение guard.
    pub fn guard(&self, user: Uuid) -> ReceiverGuard {
        ReceiverGuard {
            sse: self.clone(),
            user,
            receiver: None,
        }
    }

    /// Подписка на события.
    fn subscribe(&self, user: Uuid) -> broadcast::Receiver<EventMsg> {
        let mut map = self.channels.lock().expect("Map lock");

        if let Some(tx) = map.get(&user) {
            tx.subscribe()
        } else {
            let (tx, rx) = broadcast::channel(32);
            map.insert(user, tx);
            rx
        }
    }

    /// Отписка от событий. Используется для очистки ресурсов (удаление лишних каналов).
    fn unsubscribe(&self, user: Uuid) {
        let mut map = self.channels.lock().expect("Map lock");

        if let Some(tx) = map.get(&user) {
            if tx.receiver_count() == 0 {
                map.remove(&user);
            }
        }
    }
}

impl ReceiverGuard {
    /// Получение &mut ссылки на канал с событиями.
    pub fn receiver(&mut self) -> &mut broadcast::Receiver<EventMsg> {
        self.receiver
            .get_or_insert_with(|| self.sse.subscribe(self.user))
    }
}

impl Drop for ReceiverGuard {
    fn drop(&mut self) {
        if self.receiver.take().is_some() {
            self.sse.unsubscribe(self.user);
        }
    }
}
