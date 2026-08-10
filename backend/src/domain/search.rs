use std::collections::HashSet;

pub const NGRAM_SIZE: usize = 3;

pub type Ngram = [u8; NGRAM_SIZE];
pub type Ngrams = HashSet<Ngram>;

pub trait SearchTrigramItem: Sized {
    fn gen_ngram(&self, res: &mut Ngrams);

    fn sort_array<Other>(&self, arr: &mut [Other])
    where
        Other: SearchTrigramItem,
    {
        let mut base = Ngrams::new();
        let mut other = Ngrams::new();
        self.gen_ngram(&mut base);

        arr.sort_by_cached_key(|x| {
            other.clear();
            x.gen_ngram(&mut other);
            cmp_trigram(&base, &other)
        });

        arr.reverse();
    }
}

fn cmp_trigram(a: &Ngrams, b: &Ngrams) -> usize {
    let all = a.len() + b.len();
    if all == 0 {
        return 0;
    }

    let ok = a.intersection(b).count();
    ok * (usize::MAX / all)
}

impl<'a> SearchTrigramItem for &'a [u8] {
    fn gen_ngram(&self, res: &mut Ngrams) {
        if self.is_empty() {
            return;
        }

        let next = |x: &mut Ngram| {
            for i in 1..x.len() {
                x[i - 1] = x[i];
            }
        };

        let mut sub = [0u8; NGRAM_SIZE];

        for t in self.iter() {
            next(&mut sub);
            sub[NGRAM_SIZE - 1] = *t;
            res.insert(sub.clone());
        }

        for _ in 0..NGRAM_SIZE {
            next(&mut sub);
            res.insert(sub.clone());
        }
    }
}

impl<'a> SearchTrigramItem for &'a str {
    fn gen_ngram(&self, res: &mut Ngrams) {
        self.as_bytes().gen_ngram(res);
    }
}

impl SearchTrigramItem for String {
    fn gen_ngram(&self, res: &mut Ngrams) {
        self.as_bytes().gen_ngram(res);
    }
}
