//! Модуль с алгоритмами для вычисления долгов в комнате.
//!
//! ## Общая идея
//!
//! На основе данных о пользователях в комнате, а именно:
//! 1. Порциях
//! 2. Переводах
//! 3. Тратах
//!
//! Подсчитывается статистика о пользователе за определенный период.
//!
//! На основе этой статистики (а также учета удаленных пользователей)
//! можно посчитать вклад каждого участника - это сумма того, что
//! он вложил в комнату минус то, что он получил от комнаты.
//!
//! Данный вклад в комнате должен быть примерно одинаковым для
//! каждого пользователя.
//! Для этого можно посчитать среднее значение и с помощью
//! переводов привести значения каждого участника к среднему.
//!
//! Таким образом:
//! - `calc_stats` считает статистику по пользователям,
//! - `accounting` вычисляет оптимальное число переводов для усреднения вклада.
//!
//!
//! ## Система порций
//!
//! Иногда участник комнаты не ест в комнате, причем, возможно, достаточно долго
//! (уехал на неделю). Чтобы он за это время не платил за еду, но платил за какие-то
//! более глобальные покупки (покупка техники, ремонт и тд), существует система порций.
//!
//! На каждый день указывается число порций каждого участника.
//!
//! Тогда
//!
//! $$price = \frac{\sum food_purchase}{\sum portion_cnt}$$
//!
//! Где суммы берутся за последние 30 дней.
//!
//! Тогда получим `food_spent` - кто сколько наел еды. Это произведение числа
//! порций учатника в этот день на стоимость порции в этот день.
//! Каждый день стоимость порции может быть разной.
use chrono::{DateTime, Duration, NaiveDate, Utc};
use std::{
    cmp::min,
    collections::{BTreeSet, HashMap},
    ops::{AddAssign, SubAssign},
};
use uuid::Uuid;

/// Количество дней, за время которых вычисляется стоимость порции.
const PRICE_PERIOD: Duration = Duration::days(30);

/// Статистика по пользователю.
#[derive(Clone, Copy)]
pub struct MemberStat {
    pub id: Uuid,

    pub portions: i64,
    pub food_purchases: i64,
    pub not_food_purchases: i64,
    pub transactions: i64,
    pub food_spent: i64,
}

/// Дельта. Говорит о том, насколько определенный пользователь
/// имеет вклад больше/меньше среднего.
#[derive(Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
struct Delta {
    id: Uuid,

    val: i64,
}

/// Результат алгоритма по минимизации транзакций.
///
/// Показывает необходимость перевода от человека
/// к человеку для усреднения баланса.
#[derive(Clone, Copy)]
pub struct NeedTransaction {
    pub member_from: Uuid,
    pub member_to: Uuid,
    pub money: i64,
}

/// Описание одной порции.
#[derive(Clone, Copy)]
pub struct Portion {
    pub member: Uuid,

    pub date: NaiveDate,
    pub cnt: i64,
}

/// Описание одной транзакции.
#[derive(Clone, Copy)]
pub struct Transaction {
    pub member_to: Uuid,
    pub member_from: Uuid,

    pub money: i64,
}

/// Описание одной траты.
#[derive(Clone, Copy)]
pub struct Purchases {
    pub member: Uuid,

    pub money: i64,
    pub is_food: bool,

    pub date: NaiveDate,
}

/// Алгоритм, вычисляющий минимальное число переводов
/// для баланса вкладов.
pub fn accounting(members: &[MemberStat]) -> Vec<NeedTransaction> {
    // Для пустого числа пользователей ответ пустой.
    if members.is_empty() {
        return Vec::new();
    }

    // Считаем сумму вкладов, средний вклад, остаток.
    // Остаток необходим, так как ровно rem пользователей
    // должны иметь среднее на 1 больше, чтобы получить сумму total
    // total = cnt * mean + rem = rem * (mean + 1) + (cnt - rem) * mean
    let total: i64 = members.iter().map(|x| x.total()).sum();
    let cnt = members.len() as i64;
    let mean = total.div_euclid(cnt);
    let rem = total.rem_euclid(cnt);

    // Алгоритм работает следующим образом:
    // - Итеративно берем учатника с минимальным и максимальным вкладом (<0 и >0).
    // - Делаем между ними макс. возможный перевод.
    // - У одного из участников delta = 0 - вклад средний.
    let mut s: BTreeSet<_> = members
        .iter()
        .enumerate()
        .map(|(i, x)| x.delta(mean, i as i64, rem))
        .filter(|x| !x.is_zero())
        .collect();

    let mut res = Vec::with_capacity(members.len() - 1);
    while s.len() >= 2 {
        let mut a = s.pop_first().unwrap();
        let mut b = s.pop_last().unwrap();
        let money = a.from_to_money(b);

        res.push(NeedTransaction {
            member_from: a.id,
            member_to: b.id,
            money,
        });

        a += money;
        b -= money;
        if !a.is_zero() {
            s.insert(a);
        }
        if !b.is_zero() {
            s.insert(b);
        }
    }

    res
}

/// Алгоритм для вычисления статистики.
///
/// Подаются данные за определенный период,
/// на основе чего считается статистика.
pub fn calc_stats(
    members: &[Uuid],
    mut portions: Vec<Portion>,
    trs: &[Transaction],
    mut prs: Vec<Purchases>,
) -> Vec<MemberStat> {
    portions.sort_by_key(|x| x.date);
    prs.sort_by_key(|x| x.date);

    let mut res: HashMap<_, _> = members.iter().map(|x| (x, MemberStat::new(*x))).collect();

    for p in &portions {
        res.get_mut(&p.member).unwrap().portions += p.cnt;
    }
    for t in trs {
        res.get_mut(&t.member_from).unwrap().transactions += t.money;
        res.get_mut(&t.member_to).unwrap().transactions -= t.money;
    }
    for p in &prs {
        let x = res.get_mut(&p.member).unwrap();
        if p.is_food {
            x.food_purchases += p.money;
        } else {
            x.not_food_purchases += p.money;
        }
    }

    if !portions.is_empty() {
        let mut port_i = 0;
        let mut port_j = 0;

        let mut purch_i = 0;
        let mut purch_j = 0;

        let date = portions[0].date;

        let mut port_per_member: HashMap<_, _> = members.iter().map(|x| (x, 0i64)).collect();
        let mut all_portions = 0;
        let mut all_food_purch = 0;

        // Два указателя - держим окно для portions и purchases в PRICE_PERIOD дней.
        while port_j < portions.len() {
            while port_j < portions.len() && portions[port_j].date <= date {
                let id = portions[port_j].member;
                let cnt = portions[port_j].cnt;

                all_portions += cnt;
                *port_per_member.get_mut(&id).unwrap() += cnt;
                port_j += 1;
            }

            while port_i < port_j && portions[port_i].date < date - PRICE_PERIOD {
                let id = portions[port_i].member;
                let cnt = portions[port_i].cnt;

                all_portions -= cnt;
                *port_per_member.get_mut(&id).unwrap() += cnt;
                port_i += 1;
            }

            while purch_j < prs.len() && prs[port_j].date <= date {
                if !prs[purch_j].is_food {
                    continue;
                }

                all_food_purch += prs[purch_j].money;
                purch_j += 1;
            }

            while purch_i < port_j && prs[purch_i].date <= date - PRICE_PERIOD {
                if !prs[purch_i].is_food {
                    continue;
                }

                all_food_purch -= prs[purch_i].money;
                purch_i += 1;
            }

            if all_portions != 0 {
                // Каждому участнику вычисляем, сколько он потратил из комнаты в этот день.
                // Это будет число порций умножить на стоимость порции (с округлением вверх).
                for (id, x) in res.iter_mut() {
                    x.food_spent +=
                        (all_food_purch * port_per_member[id] + all_portions - 1) / all_portions;
                }
            }

            port_j += 1;
        }
    }

    res.into_iter().map(|(_, x)| x).collect()
}

impl MemberStat {
    /// Создание пустой статистики по `id` пользователя.
    fn new(id: Uuid) -> Self {
        Self {
            id,
            portions: 0,
            food_purchases: 0,
            not_food_purchases: 0,
            transactions: 0,
            food_spent: 0,
        }
    }

    /// Суммарный вклад пользователя.
    pub fn total(&self) -> i64 {
        self.food_purchases + self.not_food_purchases + self.transactions - self.food_spent
    }

    /// Получение дельты.
    ///
    /// Если idx < rem, то за среднее считается mean + 1,
    /// иначе просто mean.
    fn delta(&self, mut mean: i64, idx: i64, rem: i64) -> Delta {
        if idx < rem {
            mean += 1;
        }

        Delta {
            val: self.total() - mean,
            id: self.id,
        }
    }
}

impl Delta {
    /// Дельта 0 - вклад уже средний.
    fn is_zero(&self) -> bool {
        self.val == 0
    }

    /// Максимальный размер перевода от
    /// текущего пользователя к другому.
    ///
    /// Увеличивает вклад текущего и уменьшает вклад другого.
    fn from_to_money(&self, to: Delta) -> i64 {
        min(-self.val, to.val).min(0)
    }
}

impl AddAssign<i64> for Delta {
    fn add_assign(&mut self, rhs: i64) {
        self.val += rhs;
    }
}

impl SubAssign<i64> for Delta {
    fn sub_assign(&mut self, rhs: i64) {
        self.val -= rhs;
    }
}
