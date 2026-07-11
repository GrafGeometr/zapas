FROM docker.io/rust:1.97-alpine AS builder

RUN apk add --no-cache musl-dev && rm -rf /var/cache/apk/*

WORKDIR /app

COPY Cargo.toml Cargo.lock ./
RUN mkdir -p src && echo "fn main() {}" > src/main.rs
RUN cargo build --release
RUN rm -rf src

COPY . ./
RUN touch ./src/main.rs && cargo build --release


FROM docker.io/alpine:3.24

RUN apk add --no-cache ca-certificates && rm -rf /var/cache/apk/*
RUN adduser -S appuser

WORKDIR /app

COPY --from=builder /app/target/release/zapas ./zapas
RUN chown appuser:root ./zapas && chmod u+x ./zapas

USER appuser
EXPOSE 8000
ENTRYPOINT ["./zapas"]
