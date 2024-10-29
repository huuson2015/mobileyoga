buildscript {
    dependencies {
        // Cập nhật Google Services lên phiên bản mới nhất
        classpath("com.google.gms:google-services:4.3.14") // Hoặc phiên bản bạn muốn
    }
}

plugins {
    id("com.android.application") version "8.1.4" apply false
    // Thêm plugin Google Services
    id("com.google.gms.google-services") version "4.3.14" apply false // Plugin Google Services
}
