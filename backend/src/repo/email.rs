use crate::err::AppErr;

#[derive(Clone)]
pub struct EmailRepo {}

impl EmailRepo {
    pub fn new() -> Self {
        Self {}
    }

    pub async fn send_verify(&self, _email: &str) -> Result<(), AppErr> {
        Err(AppErr::NotImplemented)
    }
}
