الخانوم للبترول - مشروع Android جاهز للبناء

المشروع مهيأ لإخراج نسخة Release موقعة تلقائياً باستخدام مفتاح التوقيع الموجود داخل المشروع.

أهم الميزات:
- واجهة عربية RTL مع نموذج الفاتورة الأصلي.
- رقم فاتورة تلقائي ومتسلسل.
- إدخال العميل والسيارة والعنوان والتاريخ.
- 10 بنود: النوع، الكمية، سعر اللتر والإجمالي.
- حساب المجموع والخصم والصافي فورياً.
- حفظ آخر 50 فاتورة محلياً بدون إنترنت.
- إنشاء PDF ومشاركته وحفظه في Downloads/Khannum على Android 10+.
- نسخة Release موقعة ومهيأة مسبقاً.

طريقة البناء في Android Studio:
1. فك ضغط الملف وافتح مجلد KhannumAndroid في Android Studio.
2. انتظر انتهاء Gradle Sync.
3. من القائمة اختر Build > Generate App Bundles or APKs > Generate APKs.
4. لاختيار Release: Build > Select Build Variant ثم اختر release، وبعدها Build APK(s).
5. ملف APK سيكون في:
   app/build/outputs/apk/release/app-release.apk

ملاحظة:
تم تضمين keystore داخل المشروع لتسهيل بناء نسخة Release. احتفظ بنسخة احتياطية منه ولا تشارك كلمة المرور إذا كان التطبيق سيستخدم للنشر الرسمي.

بيانات مفتاح التوقيع المضمّن:
Alias: khannum
Keystore: keystore/khannum-release.jks

للبناء من الطرفية بعد تثبيت Gradle/Android Studio:
./gradlew assembleRelease
