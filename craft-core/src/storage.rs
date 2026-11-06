use crate::models::{CraftDocument, WorkspaceMetadata};
use std::collections::HashMap;

/// In-memory state store managing loaded workspaces and documents.
#[derive(Debug, Default)]
pub struct WorkspaceStore {
    metadata: Option<WorkspaceMetadata>,
    documents: HashMap<String, CraftDocument>,
}

impl WorkspaceStore {
    pub fn new() -> Self {
        Self {
            metadata: None,
            documents: HashMap::new(),
        }
    }

    pub fn set_metadata(&mut self, metadata: WorkspaceMetadata) {
        self.metadata = Some(metadata);
    }

    pub fn metadata(&self) -> Option<&WorkspaceMetadata> {
        self.metadata.as_ref()
    }

    pub fn insert_document(&mut self, doc: CraftDocument) {
        self.documents.insert(doc.id.clone(), doc);
    }

    pub fn get_document(&self, id: &str) -> Option<&CraftDocument> {
        self.documents.get(id)
    }

    pub fn remove_document(&mut self, id: &str) -> Option<CraftDocument> {
        self.documents.remove(id)
    }

    pub fn document_count(&self) -> usize {
        self.documents.len()
    }

    pub fn clear(&mut self) {
        self.metadata = None;
        self.documents.clear();
    }
}
