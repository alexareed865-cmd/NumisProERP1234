package com.numisproerp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.IOSDesign

private data class HelpSection(
    val titleUa: String,
    val titleEn: String,
    val bodyUa: String,
    val bodyEn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavHostController) {
    val sections = remember { buildHelpSections() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = tr("Назад", "Back"),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = tr("Довідка", "Help"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = AccentBlue)
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = tr(
                                "NumisProERP — облік нумізматичної колекції та торгівлі. " +
                                    "Натисніть розділ нижче, щоб розгорнути інструкцію.",
                                "NumisProERP — accounting for a numismatic collection and trade. " +
                                    "Tap a section below to expand the instructions."
                            ),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(sections) { section ->
                HelpSectionCard(section)
            }
        }
    }
}

@Composable
private fun HelpSectionCard(section: HelpSection) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(section.titleUa, section.titleEn),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr(section.bodyUa, section.bodyEn),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

private fun buildHelpSections(): List<HelpSection> = listOf(
    HelpSection(
        titleUa = "1. Початок роботи",
        titleEn = "1. Getting started",
        bodyUa = "• Головний екран показує загальний баланс, прибуток та останні операції.\n" +
            "• Меню зліва (іконка ☰) — повна навігація: Товари, Документи, Витрати, Звіти, " +
            "Постачальники, Клієнти, Списання, Історія, Налаштування.\n" +
            "• Нижня панель — швидкий доступ: Головна, Каталог, Склад, Налаштування.\n" +
            "• Зверху справа: довідка (ця сторінка) і сповіщення (низькі залишки).",
        bodyEn = "• The home screen shows total balance, profit, and recent transactions.\n" +
            "• The left drawer (☰ icon) is the full navigation: Products, Documents, Expenses, " +
            "Reports, Suppliers, Clients, Writeoff, History, Settings.\n" +
            "• The bottom bar is quick access: Home, Catalog, Stock, Settings.\n" +
            "• Top-right: Help (this page) and Notifications (low-stock alerts)."
    ),
    HelpSection(
        titleUa = "2. Каталог НБУ",
        titleEn = "2. NBU Catalog",
        bodyUa = "• «Каталог НБУ» — імпортована з Excel база ювілейних/обігових монет НБУ.\n" +
            "• Перший вхід: натисніть «Каталог НБУ (завантажити Excel)» і виберіть .xlsx.\n" +
            "• Після завантаження доступні сортування (за назвою/датою/номіналом) та фільтри по категоріях.\n" +
            "• Натисніть на монету, щоб побачити фото, серію, метал, тираж, художника тощо.",
        bodyEn = "• 'NBU Catalog' is an Excel-imported database of jubilee/circulation NBU coins.\n" +
            "• First-time use: tap 'NBU Catalog (load Excel)' and pick an .xlsx file.\n" +
            "• Once loaded, sorting (name/date/denomination) and category filters become available.\n" +
            "• Tap any coin for full details: photo, series, metal, mintage, artist, etc."
    ),
    HelpSection(
        titleUa = "3. Закупівля",
        titleEn = "3. Purchase",
        bodyUa = "• Виберіть товар з пошуку, постачальника (з пошуковим введенням), кількість і ціну.\n" +
            "• Можна додати до кошика кілька позицій і провести однією операцією.\n" +
            "• Сума додається до балансу постачальника та зменшує загальний баланс.\n" +
            "• Куплений товар одразу падає на склад.",
        bodyEn = "• Pick a product via search, a supplier (with search-as-you-type), quantity and price.\n" +
            "• You can add multiple positions to a cart and post them in one operation.\n" +
            "• The amount is added to the supplier balance and reduces the total balance.\n" +
            "• The purchased item immediately appears in stock."
    ),
    HelpSection(
        titleUa = "4. Продаж",
        titleEn = "4. Sale",
        bodyUa = "• Виберіть товар (тільки з тих, що в наявності), клієнта, кількість і ціну.\n" +
            "• Сума зараховується клієнту і збільшує загальний баланс.\n" +
            "• Чистий прибуток = ціна продажу − середня закупочна ціна (для товарів з колекції — без віднімання).",
        bodyEn = "• Pick a product (only those with stock), a client, quantity and price.\n" +
            "• The amount is credited to the client and increases the total balance.\n" +
            "• Net profit = sale price − avg purchase price (collection items have zero cost basis)."
    ),
    HelpSection(
        titleUa = "5. Склад і Товари",
        titleEn = "5. Stock and Products",
        bodyUa = "• «Склад» (нижня панель) — тільки те, що зараз у наявності, з фільтром по категоріях.\n" +
            "• «Товари» (бокове меню) — всі товари з бази, навіть без залишку.\n" +
            "• Натисніть на товар → відкриється детальна картка з усіма параметрами та фото.",
        bodyEn = "• 'Stock' (bottom bar) — only items currently in stock, filterable by category.\n" +
            "• 'Products' (drawer) — all items in the database, even with zero stock.\n" +
            "• Tap any item to open a detail card with full specs and photo."
    ),
    HelpSection(
        titleUa = "6. Постачальники та Клієнти",
        titleEn = "6. Suppliers and Clients",
        bodyUa = "• Додавайте, редагуйте, видаляйте контрагентів. Пошук — за іменем/контактом/містом.\n" +
            "• На картці контрагента видно повну історію операцій (закупівлі / продажі) і загальну суму.\n" +
            "• Натисніть на телефон/контакт — скопіюється у буфер.",
        bodyEn = "• Add, edit, delete counterparties. Search by name/contact/city.\n" +
            "• A counterparty card shows full transaction history (purchases/sales) and totals.\n" +
            "• Tap the phone/contact field to copy it to the clipboard."
    ),
    HelpSection(
        titleUa = "7. Списання",
        titleEn = "7. Writeoff",
        bodyUa = "• Списуйте товар зі складу через бокове меню → «Списання».\n" +
            "• Виберіть товар (показуються лише з залишком > 0), кількість і причину " +
            "(Брак / Некондиція / Втрата / Пошкодження / Інше).\n" +
            "• Сума списання = кількість × середня закупочна ціна. Враховується у Витратах звіту.",
        bodyEn = "• Write off items via drawer → 'Writeoff'.\n" +
            "• Pick an item (only those with stock > 0), quantity and reason " +
            "(Defect / Substandard / Loss / Damage / Other).\n" +
            "• Writeoff amount = quantity × avg purchase price. Counted as Expenses in Reports."
    ),
    HelpSection(
        titleUa = "8. Звіти",
        titleEn = "8. Reports",
        bodyUa = "• 4 показники: Дохід, Витрати, Чистий прибуток, Залишки на складі. " +
            "Натисніть будь-яку картку — побачите детальні розрахунки.\n" +
            "• Динаміка по місяцях — поточний місяць зверху, далі вниз по спаду.\n" +
            "• Витрати у звіті об'єднують ОЗ закупівель, інші витрати, списання — без подвійного підрахунку.",
        bodyEn = "• 4 KPIs: Income, Expenses, Net profit, Stock balance. " +
            "Tap a card for detailed breakdown.\n" +
            "• Monthly dynamics — current month on top, descending.\n" +
            "• Expenses combine purchase costs, other expenses and writeoffs — no double counting."
    ),
    HelpSection(
        titleUa = "9. Витрати",
        titleEn = "9. Expenses",
        bodyUa = "• Окремий екран для записів інших витрат (оренда, реклама тощо).\n" +
            "• Доступні фільтри по категоріях і сортування за датою/сумою.",
        bodyEn = "• Separate screen for other expense records (rent, ads, etc.).\n" +
            "• Filterable by category, sortable by date/amount."
    ),
    HelpSection(
        titleUa = "10. Історія",
        titleEn = "10. History",
        bodyUa = "• Єдина стрічка операцій: Закупівля / Продаж / Списання / Витрата.\n" +
            "• Чіпи зверху фільтрують за типом, можна сортувати за датою ↑↓.\n" +
            "• Зведена картка показує сумарні надходження та витрати за відфільтрованим набором.",
        bodyEn = "• Unified feed of operations: Purchase / Sale / Writeoff / Expense.\n" +
            "• Chips on top filter by type; sortable by date ↑↓.\n" +
            "• A summary card shows total inflow and outflow for the filtered set."
    ),
    HelpSection(
        titleUa = "11. Документи (PDF)",
        titleEn = "11. Documents (PDF)",
        bodyUa = "• Бокове меню → «Документи» → «PDF звіт по операціях».\n" +
            "• Створює A4 PDF з закупівлями, продажами, списаннями, витратами та колекцією + підсумки.\n" +
            "• Кирилицю підтримано через системні шрифти. Файл зберігається у папку Downloads.",
        bodyEn = "• Drawer → 'Documents' → 'PDF operations report'.\n" +
            "• Generates an A4 PDF with purchases, sales, writeoffs, expenses, collection + totals.\n" +
            "• Cyrillic supported via system fonts. File saved to Downloads."
    ),
    HelpSection(
        titleUa = "12. Моя колекція",
        titleEn = "12. My collection",
        bodyUa = "• Окрема таблиця власних товарів. Не впливають на баланс — це не закупівля.\n" +
            "• Можна додати фото з галереї, заповнити всі параметри + опис, вказати оціночну вартість.\n" +
            "• Товар колекції падає на склад і його можна продавати — собівартість 0, увесь дохід = прибуток.",
        bodyEn = "• A separate table of own items. They do NOT affect balance — not a purchase.\n" +
            "• Add a photo from gallery, fill all specs + description, set an estimated value.\n" +
            "• Collection items appear in stock and can be sold — zero cost basis, all revenue is profit."
    ),
    HelpSection(
        titleUa = "13. Імпорт / Експорт Excel",
        titleEn = "13. Import / Export Excel",
        bodyUa = "• Налаштування → секція «Дані».\n" +
            "• «Імпорт з Excel» — підтягує товари, клієнтів, постачальників і їхню історію.\n" +
            "• «Експорт в Excel» — створює .xlsx з усіма аркушами (включно з Складом, Списаннями, Колекцією).",
        bodyEn = "• Settings → 'Data' section.\n" +
            "• 'Import from Excel' — pulls products, clients, suppliers and their history.\n" +
            "• 'Export to Excel' — creates an .xlsx with all sheets (incl. Stock, Writeoffs, Collection)."
    ),
    HelpSection(
        titleUa = "14. Тема та мова",
        titleEn = "14. Theme and language",
        bodyUa = "• Налаштування → «Тема оформлення»: Стандартна (iOS-стиль) або OlegSmile (чорно-золота).\n" +
            "• Налаштування → «Мова інтерфейсу»: Українська або English. Перемикається миттєво, без рестарту.",
        bodyEn = "• Settings → 'Theme': Default (iOS-style) or OlegSmile (black-and-gold).\n" +
            "• Settings → 'App language': Ukrainian or English. Switches instantly, no restart."
    ),
    HelpSection(
        titleUa = "15. Сповіщення",
        titleEn = "15. Notifications",
        bodyUa = "• Іконка дзвоника зверху-справа на головному екрані.\n" +
            "• Бейдж показує кількість активних сповіщень.\n" +
            "• Поточні правила: товари з залишком ≤ 3 шт. і товари з нульовим залишком, які раніше були на складі.",
        bodyEn = "• Bell icon at the top-right of the home screen.\n" +
            "• A badge shows the count of active alerts.\n" +
            "• Current rules: items with stock ≤ 3 and items that ran out (had stock, now zero)."
    )
)
