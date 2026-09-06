use serde::{Deserialize, Serialize};
use utoipa::{IntoParams, ToSchema};
use validator::Validate;

#[derive(Clone, Copy, Serialize, Deserialize, Validate, IntoParams, ToSchema)]
pub struct Pag {
    #[serde(default = "def_page")]
    #[validate(range(min = 0))]
    pub page: i64,

    #[serde(default = "def_size")]
    #[validate(range(min = 1, max = 1000))]
    pub size: i64,

    #[serde(default = "def_rev")]
    pub reverse: bool,
}

impl Pag {
    pub fn offset(&self) -> i64 {
        self.page * self.size
    }

    pub fn limit(&self) -> i64 {
        self.size
    }
}

#[derive(Serialize, ToSchema)]
pub struct PagRes<T: Serialize> {
    pub data: Vec<T>,

    pub pag: Pag,

    pub prev_page: Option<i64>,

    pub next_page: Option<i64>,
}

impl<T: Serialize> PagRes<T> {
    pub fn new(pag: Pag) -> Self {
        Self {
            data: Vec::new(),
            pag,
            prev_page: None,
            next_page: None,
        }
    }
}

const fn def_page() -> i64 {
    0
}

const fn def_size() -> i64 {
    100
}

const fn def_rev() -> bool {
    false
}
