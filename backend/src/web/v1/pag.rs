use crate::err::AppErr;
use serde::{Deserialize, Serialize};
use utoipa::{IntoParams, ToSchema};
use validator::Validate;

/// Структура для функций репозиториев, вывод
/// которых нужно приводить к пагинации.
#[derive(Clone, Copy)]
pub struct PagVal {
    pub offset: i64,
    pub limit: i64,
}

/// Пагинация.
///
/// Доступна только по страницам (номер страницы и ее размер).
/// Также можно указывать направление (стандартное или обратное).
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
///
/// Позволяет получить как и данные, так и узнать о том, а доступны ли следующая/предыдущая страницы.
#[derive(Serialize, ToSchema)]
pub struct PagRes<T: Serialize + Clone> {
    data: Vec<T>,

    pag: Pag,
    prev_page: Option<usize>,
    next_page: Option<usize>,
}

impl Pag {
    fn offset(&self) -> i64 {
        (self.page * self.size) as i64
    }

    fn limit(&self) -> i64 {
        self.size as i64
    }
}

impl<T: Serialize + Clone> PagRes<T> {
    pub fn new<F>(pag: Pag, func: F) -> Self
    where
        F: FnOnce(PagVal) -> Vec<T>,
    {
        let data = if pag.page == 0 {
            func(PagVal {
                offset: pag.offset(),
                limit: pag.limit() + 1,
            })
        } else {
            func(PagVal {
                offset: pag.offset() - pag.limit(),
                limit: pag.limit() * 2 + 1,
            })
        };

        Self::pagging(pag, data)
    }

    pub async fn new_async<F, Fut>(pag: Pag, func: F) -> Result<Self, AppErr>
    where
        F: FnOnce(PagVal) -> Fut,
        Fut: Future<Output = Result<Vec<T>, AppErr>> + Send,
    {
        let data = if pag.page == 0 {
            func(PagVal {
                offset: pag.offset(),
                limit: pag.limit() + 1,
            })
            .await?
        } else {
            func(PagVal {
                offset: pag.offset() - pag.limit(),
                limit: pag.limit() * 2 + 1,
            })
            .await?
        };

        Ok(Self::pagging(pag, data))
    }

    fn pagging(pag: Pag, mut data: Vec<T>) -> Self {
        let prev_page = if pag.page == 0 || data.is_empty() {
            None
        } else {
            data.drain(..pag.size);
            Some(pag.page - 1)
        };

        let next_page = if data.len() > pag.size {
            data.truncate(pag.size);
            Some(pag.page + 1)
        } else {
            None
        };

        Self {
            data,
            pag,
            prev_page,
            next_page,
        }
    }
}

const fn def_page() -> usize {
    0
}

const fn def_size() -> usize {
    100
}
