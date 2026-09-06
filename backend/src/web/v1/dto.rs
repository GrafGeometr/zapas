use chrono::{DateTime, Duration, NaiveDate, Utc};
use serde::{Deserialize, Serialize};
use sqlx::prelude::FromRow;
use utoipa::{IntoParams, ToSchema};
use uuid::Uuid;
use validator::{Validate, ValidationError};

#[derive(Deserialize, Validate, ToSchema)]
pub struct RegisterDto {
    #[validate(length(min = 4, max = 63), custom(function = "validate_username"))]
    #[schema(example = "bob_ross")]
    pub username: String,

    #[validate(email)]
    #[schema(example = "bob.ross@example.com")]
    pub email: Option<String>,

    #[validate(length(min = 1, max = 127))]
    #[schema(example = "bob ross")]
    pub display_name: String,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: String,

    #[validate(length(min = 0, max = 255))] // TODO
    #[schema(example = "super_password!67")]
    pub passwd: String,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct UuidDto {
    pub id: Uuid,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct LoginDto {
    #[validate(length(min = 4, max = 63), custom(function = "validate_username"))]
    #[schema(example = "bob_ross")]
    pub username: String,

    #[validate(length(min = 0, max = 255))] // TODO
    #[schema(example = "super_password!67")]
    pub passwd: String,
}

#[derive(Serialize, Deserialize, ToSchema)]
pub struct TokenDto {
    pub token: String,
}

#[derive(Serialize, ToSchema)]
pub struct UserMeDto {
    pub id: Uuid,

    #[schema(example = "bob_ross")]
    pub username: String,

    #[schema(example = "bob.ross@example.com")]
    pub email: Option<String>,

    #[schema(example = true)]
    pub verified_email: bool,

    #[schema(example = "bob ross")]
    pub display_name: String,

    #[schema(example = "")]
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct MyProfileDto {
    #[validate(length(min = 4, max = 63), custom(function = "validate_username"))]
    #[schema(example = "bob_ross")]
    pub username: Option<String>,

    #[validate(length(min = 1, max = 127))]
    #[schema(example = "bob ross")]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: Option<String>,
}

#[derive(Serialize, ToSchema)]
pub struct SessionDto {
    pub id: Uuid,
    pub ip_addr: String,
    pub last_active_at: DateTime<Utc>,
    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct PasswdDto {
    #[validate(length(max = 255))]
    pub old_passwd: String,

    #[validate(length(min = 0, max = 255))] // TODO
    pub passwd: String,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct EmailDto {
    #[validate(email)]
    pub email: String,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct MakeRoomDto {
    #[validate(length(min = 1, max = 127))]
    #[schema(example = "502")]
    pub display_name: String,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: String,
}

#[derive(Clone, Serialize, ToSchema)]
pub struct RoomProfileDto {
    pub id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<Duration>,
    pub expired_notify: Option<Duration>,

    pub created_at: DateTime<Utc>,
}

pub struct RoomProfileDb {
    pub id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<i64>,
    pub expired_notify: Option<i64>,

    pub created_at: DateTime<Utc>,
}

impl From<RoomProfileDb> for RoomProfileDto {
    fn from(x: RoomProfileDb) -> Self {
        Self {
            id: x.id,
            display_name: x.display_name,
            bio: x.bio,
            review_interval: x.review_interval.map(|x| Duration::seconds(x)),
            expired_notify: x.expired_notify.map(|x| Duration::seconds(x)),
            created_at: x.created_at,
        }
    }
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct UpdRoomProfileDto {
    #[validate(length(min = 1, max = 127))]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    pub bio: Option<String>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct SetTimingDto {
    #[validate(range(min = 0))]
    pub review_interval: Option<i64>,
    #[validate(range(min = 0))]
    pub expired_notify: Option<i64>,
}

#[derive(Serialize, ToSchema)]
pub struct MemberDto {
    pub id: Uuid,
    pub user_id: Option<Uuid>,
    pub room_id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub created_at: DateTime<Utc>,
}

#[derive(Serialize, ToSchema)]
pub struct MemberOriginDto {
    pub id: Uuid,
    pub user_id: Option<Uuid>,
    pub room_id: Uuid,

    pub display_name: Option<String>,
    pub bio: Option<String>,

    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, ToSchema, Validate)]
pub struct SetMemberProfileDto {
    #[validate(length(min = 1, max = 63))]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    pub bio: Option<String>,
}

#[derive(Serialize, Deserialize, ToSchema, Validate)]
pub struct PortionsDto {
    #[validate(range(min = 0))]
    pub portions: i64,
}

fn validate_username(username: &str) -> Result<(), ValidationError> {
    if username
        .chars()
        .all(|c| c.is_ascii_lowercase() || c.is_ascii_digit() || c == '_')
    {
        Ok(())
    } else {
        Err(ValidationError::new("invalid username chars"))
    }
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct MakeStorageDto {
    #[validate(length(min = 1, max = 127))]
    #[schema(example = "fridge")]
    pub display_name: String,

    #[validate(length(max = 1023))]
    #[schema(example = "")]
    pub bio: String,
}

#[derive(Serialize, ToSchema)]
pub struct StorageDto {
    pub id: Uuid,
    pub room_id: Uuid,
    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<Duration>,
    pub expired_notify: Option<Duration>,

    pub created_at: DateTime<Utc>,
}

pub struct StorageDb {
    pub id: Uuid,
    pub room_id: Uuid,
    pub display_name: String,
    pub bio: String,

    pub review_interval: Option<i64>,
    pub expired_notify: Option<i64>,

    pub created_at: DateTime<Utc>,
}

impl From<StorageDb> for StorageDto {
    fn from(x: StorageDb) -> Self {
        Self {
            id: x.id,
            room_id: x.room_id,
            display_name: x.display_name,
            bio: x.bio,
            review_interval: x.review_interval.map(|x| Duration::seconds(x)),
            expired_notify: x.expired_notify.map(|x| Duration::seconds(x)),
            created_at: x.created_at,
        }
    }
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct UpdStorageProfileDto {
    #[validate(length(min = 1, max = 127))]
    pub display_name: Option<String>,

    #[validate(length(max = 1023))]
    pub bio: Option<String>,
}

#[derive(Deserialize, Validate, ToSchema)]
pub struct NewItemDto {
    #[validate(length(min = 1, max = 127))]
    pub display_name: String,

    #[validate(length(max = 1023))]
    pub bio: String,
}

#[derive(Serialize, Deserialize, sqlx::Type, ToSchema, PartialEq, Eq)]
#[serde(untagged, rename_all = "snake_case")]
#[sqlx(type_name = "amount_tp", rename_all = "snake_case")]
pub enum AmountType {
    None,
    Few,
    Mid,
    Many,
    G,
    Ml,
    Pcs,
}

impl AmountType {
    pub fn as_str(&self) -> &'static str {
        match self {
            AmountType::None => "none",
            AmountType::Few => "few",
            AmountType::Mid => "mid",
            AmountType::Many => "many",
            AmountType::G => "g",
            AmountType::Ml => "ml",
            AmountType::Pcs => "pcs",
        }
    }
}

#[derive(Serialize, ToSchema, FromRow)]
pub struct ItemDto {
    pub id: Uuid,
    pub storage_id: Uuid,

    pub display_name: String,
    pub bio: String,

    pub amount: i64,
    pub amount_type: AmountType,

    pub review_interval: Option<i64>,
    pub needed_review: bool,
    pub last_review_at: DateTime<Utc>,

    pub expired_notify: Option<i64>,
    pub expired_at: Option<DateTime<Utc>>,

    pub created_at: DateTime<Utc>,
}

#[derive(Deserialize, Validate, ToSchema)]
#[validate(schema(function = "review_dto_check"))]
pub struct ReviewDto {
    #[validate(range(min = 0))]
    pub amount: i64,

    pub amount_type: AmountType,

    pub expired_at: Option<DateTime<Utc>>,
}

fn review_dto_check(x: &ReviewDto) -> Result<(), validator::ValidationError> {
    use validator::ValidationError;

    match x.amount_type {
        AmountType::None | AmountType::Few | AmountType::Mid | AmountType::Many => {
            if x.amount != 0 {
                Err(ValidationError::new("amount not 0 with amount_type"))
            } else {
                Ok(())
            }
        }
        _ => Ok(()),
    }
}

#[derive(Deserialize, ToSchema)]
#[serde(untagged, rename_all = "snake_case")]
pub enum ItemFindFilter {
    All,
    NoneOnly,
    NotNoneOnly,
    ReviewOnly,
    ExpiredOnly,
}

#[derive(Deserialize, Validate, IntoParams)]
pub struct ItemFindPagination {
    #[validate(length(max = 127))]
    pub search: String,

    pub filter: ItemFindFilter,

    #[validate(range(min = 1, max = 10_000))]
    pub limit: i64,
}

impl ItemFindPagination {
    pub fn filter(&self, mut val: Vec<ItemDto>) -> Vec<ItemDto> {
        use crate::domain::search::*;

        match self.filter {
            ItemFindFilter::All => {}

            ItemFindFilter::NoneOnly => val.retain(|x| x.amount_type == AmountType::None),

            ItemFindFilter::NotNoneOnly => val.retain(|x| x.amount_type != AmountType::None),

            ItemFindFilter::ReviewOnly => val.retain(|x| x.needed_review),

            ItemFindFilter::ExpiredOnly => {
                let now = Utc::now();
                val.retain(|x| {
                    if let Some(exp) = x.expired_at {
                        exp <= now
                    } else {
                        false
                    }
                })
            }
        }

        self.search.sort_array(&mut val);
        if val.len() > self.limit as usize {
            val.truncate(self.limit as usize);
        }

        val
    }
}

impl crate::domain::search::SearchTrigramItem for ItemDto {
    fn gen_ngram(&self, res: &mut crate::domain::search::Ngrams) {
        self.display_name.gen_ngram(res);
    }
}

#[derive(Serialize, Deserialize, ToSchema, Validate)]
pub struct PortionDateDto {
    #[validate(range(min = 0))]
    pub portions: i64,

    pub date: NaiveDate,
}

#[derive(Deserialize, ToSchema, Validate)]
#[validate(schema(function = "check_two_date"))]
pub struct TwoDate {
    pub begin: NaiveDate,
    pub end: NaiveDate,
}

fn check_two_date(x: &TwoDate) -> Result<(), validator::ValidationError> {
    if x.begin > x.end {
        Err(validator::ValidationError::new("invalid seq"))
    } else {
        Ok(())
    }
}

#[derive(Deserialize, ToSchema, Validate)]
pub struct MakeTransactionDto {
    pub member_to: Uuid,

    #[validate(range(min = 1))]
    pub money: i64,

    #[validate(length(max = 1023))]
    pub bio: String,
}

#[derive(Serialize, ToSchema)]
pub struct TransactionDto {
    pub id: Uuid,

    pub member_from: Uuid,
    pub member_to: Uuid,

    pub money: i64,

    pub bio: String,

    pub created_at: DateTime<Utc>,
}
