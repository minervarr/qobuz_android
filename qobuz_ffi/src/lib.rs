use std::ffi::{CStr, CString, c_char, c_void};
use std::panic::{self, AssertUnwindSafe};

use qobuz_api::api::service::QobuzApiService;

/// Must be called once from android_main before any network calls.
/// Pass `app->activity->env` (JNIEnv*) and `app->activity->clazz` (jobject / Context).
///
/// # Safety
/// Both pointers must be valid for the duration of the call.
#[cfg(target_os = "android")]
#[unsafe(no_mangle)]
pub unsafe extern "C" fn qobuz_init_android(raw_vm: *mut c_void, raw_context: *mut c_void) {
    use jni::{JavaVM, objects::JObject};
    let jvm = unsafe { JavaVM::from_raw(raw_vm.cast()) };
    let _ = jvm.attach_current_thread(|env| -> Result<(), jni::errors::Error> {
        let context = unsafe { JObject::from_raw(env, raw_context.cast()) };
        rustls_platform_verifier::android::init_with_env(env, context)
    });
}

/// Searches Qobuz for Grimes albums and returns the result as a C string.
/// The caller must free it with [`qobuz_free_string`].
///
/// # Safety
/// All pointer arguments must be valid non-null null-terminated C strings.
#[unsafe(no_mangle)]
pub unsafe extern "C" fn qobuz_search_grimes(
    app_id: *const c_char,
    app_secret: *const c_char,
    user_id: *const c_char,
    auth_token: *const c_char,
) -> *mut c_char {
    let result = panic::catch_unwind(AssertUnwindSafe(|| {
        unsafe { search_inner(app_id, app_secret, user_id, auth_token) }
            .unwrap_or_else(|e| format!("Error: {e}"))
    }))
    .unwrap_or_else(|e| {
        if let Some(s) = e.downcast_ref::<String>() {
            format!("Panic: {s}")
        } else if let Some(s) = e.downcast_ref::<&str>() {
            format!("Panic: {s}")
        } else {
            "Panic: (no message)".to_string()
        }
    });

    CString::new(result)
        .unwrap_or_else(|_| CString::new("Error: result contained null byte").unwrap())
        .into_raw()
}

/// Frees a string returned by [`qobuz_search_grimes`].
///
/// # Safety
/// `s` must have been returned by [`qobuz_search_grimes`] and not already freed.
#[unsafe(no_mangle)]
pub unsafe extern "C" fn qobuz_free_string(s: *mut c_char) {
    if !s.is_null() {
        unsafe { drop(CString::from_raw(s)) };
    }
}

unsafe fn search_inner(
    app_id: *const c_char,
    app_secret: *const c_char,
    user_id: *const c_char,
    auth_token: *const c_char,
) -> Result<String, Box<dyn std::error::Error>> {
    let app_id = unsafe { CStr::from_ptr(app_id) }.to_str()?;
    let app_secret = unsafe { CStr::from_ptr(app_secret) }.to_str()?;
    let user_id = unsafe { CStr::from_ptr(user_id) }.to_str()?;
    let auth_token = unsafe { CStr::from_ptr(auth_token) }.to_str()?;

    let mut service = QobuzApiService::with_credentials(app_id, app_secret)?;
    service.login_with_token(user_id, auth_token)?;

    let results = service.search_albums("Grimes", Some(20), None)?;
    let items = results.items.unwrap_or_default();

    let mut output = format!("Albums by Grimes ({} results):\n", items.len());
    for album in &items {
        let title = album.title.as_deref().unwrap_or("?");
        let artist = album
            .artist
            .as_ref()
            .and_then(|a| a.name.as_deref())
            .unwrap_or("?");
        let year = album.release_date_original.as_deref().unwrap_or("?");
        output.push_str(&format!("  {} — {} ({})\n", title, artist, year));
    }

    Ok(output)
}
