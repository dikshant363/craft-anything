use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Metadata describing a Craft Anything workspace.
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct WorkspaceMetadata {
    pub id: String,
    pub name: String,
    pub root_uri: String,
    pub created_at_epoch_ms: u64,
    pub updated_at_epoch_ms: u64,
    pub attributes: HashMap<String, String>,
}

impl WorkspaceMetadata {
    pub fn new(id: impl Into<String>, name: impl Into<String>, root_uri: impl Into<String>) -> Self {
        let now = 0; // Epoch timestamp can be supplied by caller or host
        Self {
            id: id.into(),
            name: name.into(),
            root_uri: root_uri.into(),
            created_at_epoch_ms: now,
            updated_at_epoch_ms: now,
            attributes: HashMap::new(),
        }
    }
}

/// A document managed by the Craft Anything engine.
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct CraftDocument {
    pub id: String,
    pub title: String,
    pub mime_type: String,
    pub content: String,
    pub uri: Option<String>,
}

impl CraftDocument {
    pub fn new(id: impl Into<String>, title: impl Into<String>, content: impl Into<String>) -> Self {
        Self {
            id: id.into(),
            title: title.into(),
            mime_type: "text/plain".to_string(),
            content: content.into(),
            uri: None,
        }
    }
}

/// Status report returned to the Android host.
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct EngineStatus {
    pub version: String,
    pub is_ready: bool,
    pub active_workspace: Option<String>,
    pub document_count: usize,
}
