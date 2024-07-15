## 什么是 PR Collection

PR Collection，由 HMCL 主要开发者之一 Burning_TNT
维护，合并了大量来自 [HMC-dev/HMCL](https://github.com/HMCL-dev/HMCL/pulls) 主线的 PR 供大家测试体验。
本版本和普通快照版本不同，带有额外的自动更新功能，大家无需手动检查更新。

## 发布页面

https://zkitefly.github.io/HMCL-Snapshot-Update/prs
感谢另一位 HMCL 主要开发者 Zkitefly

## 压制警告

PR Collection 启动时，默认会提示以下警告信息：

```
你正在使用非官方的 HMCL 版本，包含许多测试中的新功能和错误修复。\n\
本应用的名称仍然为 HMCL，这是处于兼容性的考虑。\n\
如果你不知道自己在做什么，请立刻关闭本应用，并下载官方版本！请仅在你已经确保理解了本段文字的情况下继续使用本应用！\n\
可前往 https://github.com/burningtnt/HMCL/pull/9 获取更多信息（包括如何压制该警告）。
```

如果你确认自己想暂时性或永久性的关闭该警告，可执行下列任意一个步骤：

- 将环境变量 `HMCL_PR_WARNING` 设定为 `ignore`
- 将 JVM 参数 `hmcl.pr.warning` 设定为 `ignore`

## PR 合并情况（每 3 小时通过 GitHub Action 自动更新）

图例：

| %s    | %s         | %s            | %s     |
|-------|------------|---------------|--------|
| Draft | Not Merged | Partly Merged | Merged |

%s

%s