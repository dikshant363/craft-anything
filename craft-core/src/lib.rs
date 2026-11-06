pub mod jni_bridge;
pub mod models;
pub mod storage;

use models::{CraftDocument, EngineStatus, WorkspaceMetadata};
use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize)]
#[serde(tag = "action", content = "payload")]
pub enum EngineRequest {
    GetStatus,
    RegisterWorkspace(WorkspaceMetadata),
    CreateDocument(CraftDocument),
}

#[derive(Debug, Serialize, PartialEq, Eq)]
#[serde(tag = "status", content = "data")]
pub enum EngineResponse {
    Ok(serde_json::Value),
    Error(String),
}

/// Process a JSON request string and produce a JSON response string.
pub fn process_json_message(input: &str) -> String {
    match serde_json::from_str::<EngineRequest>(input) {
        Ok(EngineRequest::GetStatus) => {
            let status = EngineStatus {
                version: env!("CARGO_PKG_VERSION").to_string(),
                is_ready: true,
                active_workspace: None,
                document_count: 0,
            };
            serde_json::to_string(&EngineResponse::Ok(
                serde_json::to_value(status).unwrap_or_default(),
            ))
            .unwrap_or_else(|e| format!(r#"{{"status":"Error","data":"{}"}}"#, e))
        }
        Ok(EngineRequest::RegisterWorkspace(ws)) => {
            let val = serde_json::json!({
                "registered": true,
                "workspace_id": ws.id,
                "name": ws.name
            });
            serde_json::to_string(&EngineResponse::Ok(val))
                .unwrap_or_else(|e| format!(r#"{{"status":"Error","data":"{}"}}"#, e))
        }
        Ok(EngineRequest::CreateDocument(doc)) => {
            let val = serde_json::json!({
                "created": true,
                "document_id": doc.id,
                "title": doc.title
            });
            serde_json::to_string(&EngineResponse::Ok(val))
                .unwrap_or_else(|e| format!(r#"{{"status":"Error","data":"{}"}}"#, e))
        }
        Err(err) => serde_json::to_string(&EngineResponse::Error(format!("Invalid request: {err}")))
            .unwrap_or_else(|_| r#"{"status":"Error","data":"Serialization failure"}"#.to_string()),
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use models::WorkspaceMetadata;
    use storage::WorkspaceStore;

    #[test]
    fn test_workspace_store() {
        let mut store = WorkspaceStore::new();
        assert_eq!(store.document_count(), 0);

        let ws = WorkspaceMetadata::new("ws-1", "Test Project", "content://test/tree");
        store.set_metadata(ws);
        assert!(store.metadata().is_some());
        assert_eq!(store.metadata().unwrap().name, "Test Project");

        let doc = CraftDocument::new("doc-1", "Hello World", "Content body");
        store.insert_document(doc);
        assert_eq!(store.document_count(), 1);
        assert_eq!(store.get_document("doc-1").unwrap().title, "Hello World");

        let removed = store.remove_document("doc-1");
        assert!(removed.is_some());
        assert_eq!(store.document_count(), 0);
    }

    #[test]
    fn test_process_json_status() {
        let req = r#"{"action":"GetStatus"}"#;
        let res = process_json_message(req);
        assert!(res.contains("Ok"));
        assert!(res.contains("version"));
    }

    #[test]
    fn test_process_invalid_json() {
        let req = r#"{"invalid":"payload"}"#;
        let res = process_json_message(req);
        assert!(res.contains("Error"));
    }
}
