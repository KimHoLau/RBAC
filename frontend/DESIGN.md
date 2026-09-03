# DESIGN.md — RBAC 管理系统前端设计系统

> 主题方向：**Frosted Aurora（浅色磨砂玻璃）**
> 氛围：明亮、通透、安静的高级感；浅色极光场铺底，白色磨砂玻璃面板悬浮其上。
> 签名材质：**多层浅色玻璃** —— 白色半透明底 + backdrop blur + 发丝白描边 + 顶部内高光（sheen）+ 大半径柔和环境阴影。禁止"单一 blur"式的偷懒玻璃。
> 色彩故事：近黑墨色文字压在白玻璃上，全站唯一的 action 色是深蓝（Apple Action Blue 系），无第二强调色。
> 签名瞬间：登录页为**科幻深空场景**（本地 SVG 资产：星野/星云/流星/行星弧/透视网格），登录卡在此场景下采用**深色玻璃变体**（深蓝半透明 + 白字 + 暗玻璃输入框 + CTA 蓝色辉光），与后台浅色玻璃形成"入口→工作区"的明暗对比；后台顶栏为悬浮玻璃层，页面内容从其下方模糊穿过。

参考来源：`soft-skill`（玻璃/高级材质配方）+ `apple.md`（浅色高级 token 源）。Element Plus 为组件基座，通过 CSS 变量与少量全局覆盖换肤。

---

## 1. 色彩 Token

### 背景（极光场）
| Token | 值 | 用途 |
|---|---|---|
| `--aurora-base` | `#eef2f8` | 极光场底色（冷调近白） |
| `--aurora-blue` | `rgba(147, 197, 253, 0.50)` | 光斑 · 天蓝（右上） |
| `--aurora-violet` | `rgba(196, 181, 253, 0.42)` | 光斑 · 淡紫（左中） |
| `--aurora-peach` | `rgba(254, 215, 170, 0.35)` | 光斑 · 蜜桃（右下） |
| `--aurora-mint` | `rgba(165, 243, 224, 0.30)` | 光斑 · 薄荷（左下） |

极光场 = `body` 上多层 `radial-gradient` 光斑 + 底色，`background-attachment: fixed`。静态不动画（装饰动效 = slop）。

### 文字
| Token | 值 | 用途 |
|---|---|---|
| `--ink-900` | `#1d1d1f` | 主文字（apple Near-Black Ink） |
| `--ink-500` | `#6e6e73` | 次要文字（apple Secondary Gray） |
| `--ink-300` | `#a1a1aa` | 占位/禁用提示 |

### Action 色（唯一强调色）
| Token | 值 |
|---|---|
| `--accent` | `#0071e3`（apple Action Blue，覆盖 `--el-color-primary`） |
| `--accent-hover` | `#3395ea`（对应 `--el-color-primary-light-3`） |
| `--accent-tint` | `rgba(0, 113, 227, 0.10)`（菜单激活/选中底） |

### 玻璃材质配方（签名 —— 多层，缺一不可）
```css
/* Glass surface 完整配方 */
background: linear-gradient(150deg, rgba(255,255,255,0.72), rgba(255,255,255,0.55));
backdrop-filter: blur(18px) saturate(160%);
-webkit-backdrop-filter: blur(18px) saturate(160%);
border: 1px solid rgba(255, 255, 255, 0.65);          /* rim：发丝白描边 */
box-shadow:
  inset 0 1px 0 rgba(255, 255, 255, 0.60),            /* sheen：顶部内高光 */
  0 8px 32px rgba(31, 45, 61, 0.08);                  /* 柔和环境阴影（禁深色硬阴影） */
```

### 圆角 / 间距 / 层级
- 圆角阶梯：控件 `8px` · 卡片 `14px` · 大容器/弹窗 `16px` · 胶囊 `999px`（apple 半径阶梯，禁止全站单一圆角）
- 间距：8px 基数；页面 padding 16px；卡片内 padding 20px+
- 阴影只用于玻璃面与悬浮层，深度克制（apple： tonal separation 优先）

## 2. 原语（Primitives）

| 原语 | 构成 | 用在哪 |
|---|---|---|
| `aurora-bg` | body 多层 radial-gradient，fixed | 全站背景 |
| `glass-card` | 玻璃配方 + radius 14 | `el-card`（页面卡片、统计卡、登录卡） |
| `glass-bar` | 玻璃配方（弱化阴影）+ 发丝底线 | 顶栏 `.layout-header`（悬浮层，内容从下方穿过） |
| `glass-rail` | 玻璃配方（不透明度略高）+ 发丝右边线 | 侧边栏 `el-aside` |
| `glass-pop` | 玻璃配方 + radius 12 + 更深阴影 | 下拉/对话框/MessageBox/Select 弹层 |
| `glass-input` | `rgba(255,255,255,0.55)` 底 + 发丝灰描边 | `el-input` 内层 |
| `pill-active` | `--accent-tint` 底 + `--accent` 文字 + radius 8 | 侧栏菜单激活态（胶囊几何） |
| `toolbar` | flex 行 · gap 8px · 下缘间距 16px | 列表页操作工具栏（`.toolbar`，全局原语，页面不得重复声明） |
| `pagination-bar` | 右对齐 · 上缘间距 16px | 列表页分页条（`.pagination-bar`，全局原语，页面不得重复声明） |

> 铁律：组件样式一律走 token（`styles/index.css` 定义的 CSS 变量或 EP 变量），禁止硬编码颜色/圆角/阴影 —— 硬编码值不随 `html.dark` 翻转，是双主题的头号破坏源。

## 3. 动效

- 缓动：`cubic-bezier(0.32, 0.72, 0, 1)`（自定义曲线，禁 linear / ease-in-out）
- 路由切换：`fade-slide` —— opacity 0→1 + translateY 8px→0，220ms（transform/opacity only）
- 折叠/悬停：160–200ms 同曲线
- `@media (prefers-reduced-motion: reduce)` 下全部过渡关闭
- 背景光斑**不动画**；backdrop-filter 只用于固定层（顶栏/侧栏/弹层）与少量静态卡片，禁止用于滚动中的大内容区

## 4. 排版

- 字体栈：系统优先（SF Pro / Segoe / PingFang SC / Microsoft YaHei），**不引入网络字体**（离线优先，见"接受的技术债"）
- 数字/统计值：`font-variant-numeric: tabular-nums`，weight 600，`letter-spacing: -0.02em`
- 层级：页面标题 18–20px/600 · 正文 14px · 辅助 13px `--ink-500`

## 5. 无障碍约束

- 正文对比度：`--ink-900` on 白玻璃 ≥ 12:1；次要文字 ≥ 4.5:1；占位文字不作信息承载
- 键盘焦点：保留 Element Plus 默认 focus ring（`--accent` 色）
- `prefers-reduced-motion` 全局尊重
- 玻璃不承载关键信息区分 —— 激活态同时有颜色 + 文字色变化，不只靠底色

## 7. 双主题（白天 / 黑夜）

- 机制：全部材质收敛为 `:root` token（极光场、ink 文字、玻璃 rim/sheen/hairline、输入框、面板梯度、投影阶梯），`html.dark` 仅翻转 token；组件规则不感知主题
- 切换：顶栏右侧日/月按钮（`useTheme`），`html.dark` class + `localStorage('rbac-theme')` 持久化，`main.ts` 挂载前初始化防闪白
- Element Plus：引入官方 `dark/css-vars.css` 作暗色基底，本系统在其上覆盖 accent 蓝梯度与中性色（引入顺序：EP css → EP dark css → index.css）
- 黑夜玻璃配方：面板底 `rgba(22,34,62,.62)→rgba(12,20,40,.5)`、rim `rgba(140,180,255,.16)`、sheen `rgba(255,255,255,.07)`、hover 填充 `rgba(120,160,230,.12)`、投影换黑色系
- 黑夜极光场：同构光斑布局，饱和度压低（深空蓝/紫/暖橙/薄荷各 ~0.12–0.3 alpha）
- 登录页例外：科幻深空场景固定不随主题切换（深空本身即"夜"），登录卡恒为深色玻璃
- 对比度：黑夜下 `--ink-900 #e8edf6` on 暗玻璃 ≥ 10:1；次要文字 `#9aa7bd` ≥ 4.5:1

## 8. 接受的技术债

- **无自定义展示字体**：为保持零网络依赖（项目反模式：不引入计划外依赖），Latin 字形走系统栈；若未来允许，首选引入 `Inter Tight`（apple.md 推荐替代）
- **backdrop-filter 在低端设备有成本**：卡片数量少（每页 ≤5 个静态卡），已按 soft-skill 护栏约束在固定层与静态面；不支持 `backdrop-filter` 的浏览器回退为不透明白（`@supports` 未命中时自然回退，玻璃变量层可整体关闭）
- Element Plus 弹层玻璃化覆盖依赖其 DOM 结构（`.el-popper`），EP 大版本升级需回归
- **玻璃表格禁用固定列**：`el-table-column` 不加 `fixed="left/right"` —— sticky 固定列悬浮于透明玻璃列之上，遮挡滚过内容必须带底色（透明→叠字；实底→白块），与玻璃材质根本冲突。现所有表格操作列均为普通列，窄视口下随表格横向滚动
