# JDK 26 — Speaker Notes

---

## 🎤 Opening / Release Overview

**🇬🇧 EN**
> Java 26 dropped on March 17, 2026 — right on the 6-month release train.
> 10 JEPs this time — one of the smaller releases lately, but don't let the number fool you.
> Half are finalizations, half are previews continuing to mature.
> Two "end of an era" moments: Applet API gone after 27 years of deprecation, Thread.stop() removed after... also 27 years.
> The big theme this release: the JVM is getting smarter about memory — AOT cache now works with ZGC, G1 gets 5–15% free throughput, heap starts smaller by default.
> And on the language side, features from Project Amber and Project Loom keep marching toward finalization.

**🇺🇦 UA**
> Java 26 вийшов 17 березня 2026 — строго за розкладом, кожні 6 місяців.
> 10 JEP-ів цього разу — один із менших релізів останнього часу, але не варто недооцінювати.
> Половина — фіналізації, половина — preview, що продовжують дозрівати.
> Два моменти "кінця епохи": Applet API видалено після 27 років депрекації, Thread.stop() — теж після 27 років.
> Головна тема релізу: JVM стає розумнішою з пам'яттю — AOT-кеш тепер працює з ZGC, G1 отримує 5–15% продуктивності безкоштовно, heap стартує менший за замовчуванням.
> З мовної сторони — фічі з Project Amber і Project Loom продовжують рухатись до фіналізації.

---

## 1. Primitive Types in Patterns (JEP 530) — 4th Preview

**🇬🇧 EN**
> Who here has written a chain of if-else to classify HTTP status codes?
> That's exactly what this JEP kills.
> Pattern matching now works with ALL primitive types — not just reference types.
> You can write `case int i when i >= 400 && i < 500` — and it reads like English.
> Also new: `case byte b` on a double — matches only if the value fits exactly. No boxing, no casting.
> 4th preview means it's very stable — likely final in JDK 27.
> Key change in JDK 26: dominance checks are stricter — some code that compiled in JDK 25 now gives a compile error. The compiler catches mistakes it previously let through.

**🇺🇦 UA**
> Хто з вас писав ланцюжок if-else для класифікації HTTP-статус кодів?
> Саме це і вбиває цей JEP.
> Pattern matching тепер працює з УСІМА примітивними типами — не тільки з об'єктами.
> Можна написати `case int i when i >= 400 && i < 500` — читається як звичайна мова.
> Також нове: `case byte b` для double — спрацьовує тільки якщо значення точно вміщується. Без boxing, без casting.
> 4-й preview означає дуже стабільно — скоріш за все фінал у JDK 27.
> Ключова зміна в JDK 26: строгіші dominance-перевірки — деякий код, що компілювався в JDK 25, тепер дає compile error. Компілятор ловить помилки, які раніше пропускав.

---

## 2. Prepare to Make Final Mean Final (JEP 500)

**🇬🇧 EN**
> Quick question — is a `final` field really immutable in Java?
> The answer is: not if you know how to use reflection.
> `setAccessible(true)` + `field.set()` — and your "immutable" object is mutated.
> Frameworks like Spring, Mockito, and serialization libraries have been doing this for years.
> JDK 26 is the beginning of the end for this loophole.
> By default now: a JVM WARNING the first time you mutate a final field.
> In a future JDK: `deny` becomes the default — it throws `IllegalAccessException`.
> There's also a new JFR event `jdk.FinalFieldMutation` — great for auditing your dependencies.
> Important: we used a regular class, not a record — records are already truly immutable and JDK 26 blocks them outright.

**🇺🇦 UA**
> Швидке питання — `final` поле справді незмінне в Java?
> Відповідь: ні, якщо знаєш як використовувати рефлексію.
> `setAccessible(true)` + `field.set()` — і твій "незмінний" об'єкт змінено.
> Фреймворки як Spring, Mockito та бібліотеки серіалізації робили це роками.
> JDK 26 — початок кінця для цієї лазівки.
> За замовчуванням тепер: JVM WARNING при першій спробі змінити final поле.
> У майбутньому JDK: `deny` стане типовим — буде кидати `IllegalAccessException`.
> Також є новий JFR-event `jdk.FinalFieldMutation` — корисно для аудиту залежностей.
> Важливо: ми використовуємо звичайний клас, не record — records вже справді незмінні, і JDK 26 блокує їх повністю.

---

## 3. Lazy Constants (JEP 526) — 2nd Preview

**🇬🇧 EN**
> How many of you have written Double-Checked Locking in your career?
> It's error-prone, hard to read, and easy to get wrong.
> Lazy Constants is Java's proper answer to this problem.
> Three concepts: LazyConstant for a single value, List.ofLazy for per-index lazy loading, Map.ofLazy for per-key lazy loading.
> The JVM treats the value as a true constant after initialization — same JIT optimizations as a final field.
> Thread-safe by construction — the supplier is called at most once.
> This was called "Stable Values" in JDK 25 — renamed and significantly simplified in JDK 26. If you used the JDK 25 preview, you'll need to refactor.
> Watch the output: the OrderService prints its init message only once. The ReportService never prints — never needed, never created.

**🇺🇦 UA**
> Хто з вас писав Double-Checked Locking у своїй кар'єрі?
> Це схильне до помилок, важко читається і легко зробити неправильно.
> Lazy Constants — це правильна відповідь Java на цю проблему.
> Три концепції: LazyConstant для одного значення, List.ofLazy для ліниво завантаження по індексу, Map.ofLazy по ключу.
> JVM трактує значення як справжню константу після ініціалізації — ті самі JIT-оптимізації, що і для final поля.
> Потокобезпечний за конструкцією — supplier викликається щонайбільше один раз.
> У JDK 25 це називалося "Stable Values" — перейменовано і значно спрощено в JDK 26. Якщо використовували JDK 25 preview — потрібен рефакторинг.
> Слідкуйте за виводом: OrderService виводить своє повідомлення про ініціалізацію лише один раз. ReportService не виводить нічого — не потрібен, не створений.

---

## 4. Structured Concurrency (JEP 525) — 6th Preview

**🇬🇧 EN**
> 6th preview — someone in the audience is probably wondering when this graduates.
> The API is stable; the team is carefully gathering real-world feedback before finalizing.
> Core idea: related tasks that fork together must join together. One scope, one convergence point. No orphaned threads. No zombie tasks.
> We show three patterns:
>   — allSuccessfulOrThrow: dashboard scenario, all data needed, one failure kills the rest
>   — anySuccessfulOrThrow: redundant services, first response wins, others cancelled
>   — allUntil: search scenario, stop as soon as a match is found
> Key change in JDK 26: join() now returns List, not Stream. Simpler, no lazy evaluation surprises.
> Also: anySuccessfulResultOrThrow() renamed to anySuccessfulOrThrow() — cleaner name.
> The custom Joiner interface lets you implement policies the built-ins don't cover — like "at least 2 out of 3 must succeed".

**🇺🇦 UA**
> 6-й preview — хтось у залі напевно думає, коли ж це нарешті фіналізують.
> API стабільний; команда ретельно збирає зворотній зв'язок від реального використання перед фіналізацією.
> Головна ідея: пов'язані задачі, що форкнулись разом, мають завершитись разом. Один scope, одна точка збору. Ніяких покинутих потоків. Ніяких зомбі-задач.
> Показуємо три патерни:
>   — allSuccessfulOrThrow: дашборд, потрібні всі дані, один збій вбиває решту
>   — anySuccessfulOrThrow: резервні сервіси, перша відповідь перемагає, інші скасовуються
>   — allUntil: пошук, зупинитись як тільки знайдено збіг
> Ключова зміна в JDK 26: join() тепер повертає List, а не Stream. Простіше, без сюрпризів лінивих обчислень.
> Також: anySuccessfulResultOrThrow() перейменовано на anySuccessfulOrThrow() — чистіша назва.
> Власний Joiner дозволяє реалізувати політики, яких немає у вбудованих — наприклад "мінімум 2 з 3 мають успішно завершитись".

---

## 5. HTTP/3 for the HTTP Client API (JEP 517)

**🇬🇧 EN**
> HTTP/3 has been the standard since 2022. Java 26 is finally catching up.
> HTTP/3 runs over QUIC — which runs over UDP, not TCP. Faster connection setup, better handling of packet loss.
> One new enum constant: HttpClient.Version.HTTP_3. That's the whole API surface.
> Watch what happens: the first request comes back as HTTP_2 with a 301. That's normal.
> The server says "I support HTTP/3" via the Alt-Svc header — the JVM caches this.
> Second request: HTTP_3. That's the real-world adoption pattern — same as browsers.
> Practical warning: HTTP/3 needs UDP port 443 open. Many corporate networks and conference WiFis block it. If you see connection refused — that's why.

**🇺🇦 UA**
> HTTP/3 — стандарт з 2022 року. Java 26 нарешті наздоганяє.
> HTTP/3 працює поверх QUIC — який працює поверх UDP, а не TCP. Швидше встановлення з'єднання, краща обробка втрати пакетів.
> Один новий enum-константа: HttpClient.Version.HTTP_3. Це весь новий API.
> Дивіться що відбувається: перший запит повертається як HTTP_2 з 301. Це нормально.
> Сервер каже "я підтримую HTTP/3" через заголовок Alt-Svc — JVM це кешує.
> Другий запит: HTTP_3. Це реальний патерн впровадження — так само як браузери.
> Практичне попередження: HTTP/3 потребує відкритого UDP-порту 443. Багато корпоративних мереж і конференц-WiFi блокують його. Якщо бачите connection refused — ось чому.

---

## 6. PEM Encodings (JEP 524) — 2nd Preview

**🇬🇧 EN**
> Who has dealt with PEM files? .pem, .crt, .key — they're everywhere in TLS configuration.
> Before this JEP, reading an encrypted private key from PEM in Java required 15+ lines: strip headers, Base64 decode, EncryptedPrivateKeyInfo, Cipher.init(), KeyFactory... it was painful.
> Now: one method call. PEMDecoder.of().withDecryption(passphrase).decode(pem, PrivateKey.class).
> PEMEncoder works the same way in reverse.
> New in JDK 26 vs JDK 25: you can now encode/decode an entire KeyPair, not just individual keys.
> This is a 2nd preview — minor API adjustments from the first round, but the concept is stable.

**🇺🇦 UA**
> Хто мав справу з PEM-файлами? .pem, .crt, .key — вони скрізь в TLS-конфігурації.
> До цього JEP, читання зашифрованого приватного ключа з PEM у Java вимагало 15+ рядків: прибрати заголовки, Base64 decode, EncryptedPrivateKeyInfo, Cipher.init(), KeyFactory... це було болісно.
> Тепер: один виклик методу. PEMDecoder.of().withDecryption(passphrase).decode(pem, PrivateKey.class).
> PEMEncoder працює так само у зворотному напрямку.
> Нове в JDK 26 порівняно з JDK 25: тепер можна кодувати/декодувати цілий KeyPair, а не тільки окремі ключі.
> Це 2-й preview — незначні API-коригування після першого раунду, але концепція стабільна.

---

## 7. Vector API (JEP 529) — 11th Incubator

**🇬🇧 EN**
> 11th incubator. Same as last year. And the year before.
> No API changes. So why are we still talking about it?
> Because the reason it's still incubating is actually the most interesting part.
> The benchmark you're about to see will show scalar beating vector. That's not a bug.
> The JIT already auto-vectorizes simple loops — it's doing SIMD under the hood.
> And the explicit Vector API has overhead: every FloatVector.add(), .max(), .min() creates a heap-allocated object. That GC cost outweighs the SIMD gain for simple operations.
> This is EXACTLY why it needs Valhalla. Once FloatVector becomes a value type — zero heap allocation, zero GC — the speedup will be real.
> So this demo is actually a perfect illustration of the dependency chain: Vector API → Valhalla → graduation.

**🇺🇦 UA**
> 11-й incubator. Те саме, що минулого року. І позапозаминулого.
> Ніяких змін API. То навіщо ми про це говоримо?
> Бо причина, чому він досі на incubator, насправді є найцікавішою частиною.
> Бенчмарк, який ви зараз побачите, покаже що scalar перемагає vector. Це не баг.
> JIT вже автоматично векторизує прості цикли — він робить SIMD під капотом.
> І явний Vector API має накладні витрати: кожен FloatVector.add(), .max(), .min() створює об'єкт у heap. Ця GC-вартість перевищує виграш від SIMD для простих операцій.
> Саме тому йому потрібен Valhalla. Як тільки FloatVector стане value type — нуль алокацій в heap, нуль GC — виграш буде реальним.
> Тому цей демо — ідеальна ілюстрація ланцюжка залежностей: Vector API → Valhalla → graduation.

---

## 8. AOT Object Caching with Any GC (JEP 516)

**🇬🇧 EN**
> JDK 24 introduced AOT Class Loading & Linking — cache your classes before startup, load them instantly.
> Problem: the cache format was tied to your GC. Cache built with G1? Can't use with ZGC.
> JDK 26 fixes this. Objects in the cache are stored by logical index, not memory address.
> At startup they're streamed into the heap in the GC-specific format. Works with ZGC, works with any heap size.
> Part of Project Leyden — the long-term effort to make Java startup as fast as native.
> No code change needed — just rebuild your AOT cache and use any GC you want.

**🇺🇦 UA**
> JDK 24 представив AOT Class Loading & Linking — кешуйте класи до запуску, завантажуйте їх миттєво.
> Проблема: формат кешу був прив'язаний до вашого GC. Кеш побудований з G1? Не можна використовувати з ZGC.
> JDK 26 виправляє це. Об'єкти в кеші зберігаються за логічним індексом, а не адресою пам'яті.
> При запуску вони потоково завантажуються в heap у GC-специфічному форматі. Працює з ZGC, працює з будь-яким розміром heap.
> Частина Project Leyden — довгострокові зусилля зробити запуск Java таким же швидким як нативний.
> Ніяких змін коду — просто перебудуйте AOT-кеш і використовуйте будь-який GC.

---

## 9. G1 GC: Improve Throughput (JEP 522)

**🇬🇧 EN**
> Free performance — my favourite kind.
> G1 tracks cross-region references using a Card Table. Every time your code updates a reference, a write barrier fires and marks the card.
> There's also an optimization thread constantly cleaning up that Card Table.
> Problem: both application threads and the optimizer hit the same Card Table. Constant synchronization. Constant contention.
> Fix: add a second Card Table. App threads write to one, optimizer works on the other. When the active table degrades, they swap — zero synchronization needed.
> Result: 5–15% throughput improvement. You get it for free just by upgrading to JDK 26.
> Cost: 0.2% more native memory. That's 2 MB per GB of heap — completely negligible.

**🇺🇦 UA**
> Безкоштовна продуктивність — мій улюблений вид.
> G1 відстежує міжрегіональні посилання за допомогою Card Table. Щоразу коли ваш код оновлює посилання, спрацьовує write barrier і позначає картку.
> Є також потік оптимізації, що постійно очищає цю Card Table.
> Проблема: і потоки застосунку, і оптимізатор працюють з однією Card Table. Постійна синхронізація. Постійна конкуренція.
> Рішення: додати другу Card Table. Потоки застосунку пишуть в одну, оптимізатор працює з іншою. Коли активна таблиця деградує — вони міняються місцями. Нуль синхронізації.
> Результат: 5–15% приросту продуктивності. Отримуєте безкоштовно просто оновившись до JDK 26.
> Вартість: 0.2% більше нативної пам'яті. Це 2 МБ на ГБ heap — цілком незначно.

---

## 10. Removals & Beyond JEPs

**🇬🇧 EN**
> Two "end of an era" moments worth a pause.
> Applet API: deprecated in Java 9 (2017), deprecated-for-removal in Java 17, Security Manager disabled in Java 24, now fully gone. If you still have `import java.applet` anywhere — now's the time.
> Thread.stop(): deprecated in JDK 1.2. December 1998. That's 27 years ago. It threw UnsupportedOperationException since Java 20. Now the method is simply gone.
> The right replacement is Thread.interrupt() — cooperative cancellation, not forceful termination.
> Other notable non-JEP changes:
>   — Default initial heap: was 1/64 of RAM (1 GB on a 64 GB machine for Hello World). Now 1/500. Faster startup, less waste.
>   — Virtual threads no longer pin when waiting for class initialization. Complements the JDK 24 fix for synchronized blocks.
>   — Unicode 17.0 support.
>   — Dark theme in Javadoc. Finally.

**🇺🇦 UA**
> Два моменти "кінця епохи", варті паузи.
> Applet API: депрекований у Java 9 (2017), депрекований для видалення у Java 17, Security Manager вимкнено у Java 24, тепер повністю видалено. Якщо у вас ще є `import java.applet` — час діяти.
> Thread.stop(): депрекований у JDK 1.2. Грудень 1998 року. Це 27 років тому. Кидав UnsupportedOperationException з Java 20. Тепер метод просто відсутній.
> Правильна заміна — Thread.interrupt() — кооперативне скасування, а не примусове завершення.
> Інші помітні зміни поза JEP-ами:
>   — Початковий heap за замовчуванням: був 1/64 RAM (1 ГБ на машині з 64 ГБ для Hello World). Тепер 1/500. Швидший запуск, менше марнування.
>   — Virtual threads більше не пінять потік при очікуванні ініціалізації класу. Доповнює виправлення JDK 24 для synchronized блоків.
>   — Підтримка Unicode 17.0.
>   — Темна тема в Javadoc. Нарешті.

---

## 🎤 Closing

**🇬🇧 EN**
> JDK 26 is a release about the JVM getting mature and reliable, while the language features keep building momentum toward finalization.
> Structured Concurrency, Lazy Constants, Primitive Patterns — these will be final soon. They're production-ready in spirit even as previews.
> The big story on the horizon: Project Valhalla. Value types will unblock the Vector API, improve LazyConstant JIT optimizations, and reshape how Java handles data-oriented programming.
> Try JDK 26 today — sdk install java 26-oracle. It's worth it just for the G1 throughput and heap size improvements alone.

**🇺🇦 UA**
> JDK 26 — це реліз про те, як JVM стає зрілішою і надійнішою, поки мовні фічі продовжують набирати темп до фіналізації.
> Structured Concurrency, Lazy Constants, Primitive Patterns — скоро будуть фінальними. Вони готові до продакшну за духом, навіть будучи preview.
> Велика історія на горизонті: Project Valhalla. Value types розблокують Vector API, покращать JIT-оптимізації LazyConstant, і змінять підхід Java до data-oriented програмування.
> Спробуйте JDK 26 вже сьогодні — sdk install java 26-oracle. Воно того варте навіть тільки заради покращень продуктивності G1 та розміру heap.
