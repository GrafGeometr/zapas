use tracing::warn;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    init_fmt();

    Ok(())
}

fn init_fmt() {
    println!(
        r#"
-----------------------------------------------------------

╺━┓┏━┓┏━┓┏━┓┏━┓
┏━┛┣━┫┣━┛┣━┫┗━┓
┗━╸╹ ╹╹  ╹ ╹┗━┛

version: {}

-----------------------------------------------------------

"#,
        env!("CARGO_PKG_VERSION")
    );

    tracing_subscriber::fmt()
        .with_target(true)
        .with_file(true)
        .with_line_number(true)
        .with_max_level(tracing::Level::TRACE)
        .pretty()
        .init();
    warn!("Session is coming");
}
