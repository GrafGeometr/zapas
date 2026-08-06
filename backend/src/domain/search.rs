use std::collections::HashSet;

pub type Gram = [u8; 3];
pub type Grams = HashSet<Gram>;

pub trait SearchTrigramItem: Sized {
    fn gen_trigram(&self, res: &mut Grams);
}

pub fn cmp_trigram(a: &Grams, b: &Grams) -> usize {
    let all = a.len() + b.len();
    if all == 0 {
        return 0;
    }

    let ok = a.intersection(b).count();
    ok * (usize::MAX / all)
}

pub fn sort_array<A, B>(a: &A, arr: &mut [B])
where
    A: SearchTrigramItem,
    B: SearchTrigramItem + Sized,
{
    let mut base = Grams::new();
    let mut val = Grams::new();
    a.gen_trigram(&mut base);
    arr.sort_by_cached_key(|x| {
        val.clear();
        x.gen_trigram(&mut val);
        cmp_trigram(&base, &val)
    });
    arr.reverse();
}

impl<'a> SearchTrigramItem for &'a str {
    fn gen_trigram(&self, res: &mut Grams) {
        let bytes = self.as_bytes();

        if bytes.is_empty() {
            return;
        }

        if bytes.len() == 1 {
            res.insert([0, bytes[0], 0]);
        } else {
            // >= 2
            res.insert([0, 0, bytes[0]]); // 0 0 x
            res.insert([0, bytes[0], bytes[1]]); // 0 x x

            let end = bytes.len();
            res.insert([bytes[end - 1], 0, 0]); // x 0 0
            res.insert([bytes[end - 2], bytes[end - 1], 0]); // x x 0

            // x x x
            for i in 0..bytes.len() - 2 {
                res.insert([bytes[i], bytes[i + 1], bytes[i + 2]]);
            }
        }
    }
}

impl SearchTrigramItem for String {
    fn gen_trigram(&self, res: &mut Grams) {
        (&self[..]).gen_trigram(res);
    }
}
