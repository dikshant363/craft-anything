#[cfg(feature = "jni-export")]
use jni::objects::{JClass, JString};
#[cfg(feature = "jni-export")]
use jni::sys::jstring;
#[cfg(feature = "jni-export")]
use jni::JNIEnv;

use std::ffi::{CStr, CString};
use std::os::raw::c_char;

/// C-ABI function to return the core engine version string.
#[no_mangle]
pub extern "C" fn craft_core_version() -> *const c_char {
    static VERSION: &str = concat!(env!("CARGO_PKG_VERSION"), "\0");
    VERSION.as_ptr() as *const c_char
}

/// C-ABI function to process a JSON request payload and return a JSON response.
/// Caller must free the returned string using `craft_core_free_string`.
#[no_mangle]
pub extern "C" fn craft_core_process_payload(json_ptr: *const c_char) -> *mut c_char {
    if json_ptr.is_null() {
        return std::ptr::null_mut();
    }

    let c_str = unsafe { CStr::from_ptr(json_ptr) };
    let input = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return std::ptr::null_mut(),
    };

    let response = crate::process_json_message(input);
    match CString::new(response) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// C-ABI function to free strings returned by `craft_core_process_payload`.
#[no_mangle]
pub extern "C" fn craft_core_free_string(ptr: *mut c_char) {
    if !ptr.is_null() {
        unsafe {
            let _ = CString::from_raw(ptr);
        }
    }
}

// JNI Entry Points (Active when jni-export feature is enabled)
#[cfg(feature = "jni-export")]
#[no_mangle]
pub extern "system" fn Java_dev_craftanything_app_core_NativeBridge_getEngineVersion<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jstring {
    let version = env!("CARGO_PKG_VERSION");
    let output = env.new_string(version).expect("Failed to create Java string");
    output.into_raw()
}

#[cfg(feature = "jni-export")]
#[no_mangle]
pub extern "system" fn Java_dev_craftanything_app_core_NativeBridge_processPayload<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    input: JString<'local>,
) -> jstring {
    let input_str: String = match env.get_string(&input) {
        Ok(js) => js.into(),
        Err(_) => return std::ptr::null_mut(),
    };

    let result = crate::process_json_message(&input_str);
    let output = env.new_string(result).expect("Failed to create Java string");
    output.into_raw()
}
