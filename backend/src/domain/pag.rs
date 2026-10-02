//! Модуль для стандартного средства пагинации.
use serde::{Deserialize, Serialize};
use utoipa::{IntoParams, ToSchema};
use validator::Validate;

/// Аргумент для функций с поддержкой пагинации.
/// Дает необходимые аргументы для `sql` запроса.
#[derive(Clone, Copy)]
pub struct PagArg {
    pub offset: i64,
    pub limit: i64,
}

/// Структура с необходимыми данными для пагинации.
#[derive(Clone, Copy, Serialize, Deserialize, Validate, IntoParams, ToSchema)]
pub struct Pag {
    #[serde(default = "def_page")]
    #[validate(range(min = 0))]
    page: usize,

    #[serde(default = "def_size")]
    #[validate(range(min = 1, max = 1_000))]
    size: usize,
}

/// Результат пагинации.
#[derive(Serialize, ToSchema)]
pub struct PagRes<T: Serialize + ToSchema + Clone> {
    data: Vec<T>,

    pag: Pag,
    prev_page: Option<usize>,
    next_page: Option<usize>,

    has_prev: bool,
    has_next: bool,
}

const fn def_page() -> usize {
    0
}

const fn def_size() -> usize {
    100
}

impl<T: Serialize + ToSchema + Clone> PagRes<T> {
    /// Пагинация для простой функции.
    pub fn new<F>(pag: Pag, f: F) -> Self
    where
        F: FnOnce(PagArg) -> Vec<T>,
    {
        Self::pagging(pag, (f)(pag.make_arg()))
    }

    /// Пагинация для функции с возможной ошибкой.
    pub fn new_with_err<F, E>(pag: Pag, f: F) -> Result<Self, E>
    where
        F: FnOnce(PagArg) -> Result<Vec<T>, E>,
    {
        Ok(Self::pagging(pag, (f)(pag.make_arg())?))
    }

    /// Пагинация для асинхронной функции с возможной ошибкой.
    pub async fn new_async_with_err<F, Fut, E>(pag: Pag, f: F) -> Result<Self, E>
    where
        F: FnOnce(PagArg) -> Fut,
        Fut: Future<Output = Result<Vec<T>, E>> + Send,
    {
        Ok(Self::pagging(pag, (f)(pag.make_arg()).await?))
    }

    /// Логика для построения результата.
    fn pagging(pag: Pag, mut data: Vec<T>) -> Self {
        let prev_page = if pag.page == 0 || data.is_empty() {
            None
        } else {
            data.remove(0);
            Some(pag.page - 1)
        };

        let next_page = if data.len() > pag.size {
            data.pop();
            Some(pag.page + 1)
        } else {
            None
        };

        Self {
            data,
            pag,
            prev_page,
            next_page,
            has_prev: prev_page.is_some(),
            has_next: next_page.is_some(),
        }
    }
}

impl Pag {
    fn make_arg(&self) -> PagArg {
        if self.page == 0 {
            PagArg {
                offset: 0,
                limit: self.size as i64 + 1,
            }
        } else {
            PagArg {
                offset: (self.page * self.size) as i64 - 1,
                limit: self.size as i64 + 2,
            }
        }
    }
}
