# 自定义 Schema 说明

## MetaSchema （方言）
> **MetaSchema** 用于自定义约束，即引即用。
* [https://api.json-schema.cloud/v1/schemas/form](https://api.json-schema.cloud/v1/schemas/form) —— 约束 Form 结构
* [https://api.json-schema.cloud/v1/schemas/schema](https://api.json-schema.cloud/v1/schemas/schema) —— 约束 Form 中的 properties
* [https://api.json-schema.cloud/v1/schemas/record](https://api.json-schema.cloud/v1/schemas/record) —— 引用记录结构


使用
[https://api.json-schema.cloud/v1/schemas/form](https://api.json-schema.cloud/v1/schemas/form)
校验 Form 格式是否正确。
而 Form 本身用于校验 Record 格式是否正确。

## 示例
### 客户信息 Schema
```json
{
  "$id": "https://api.json-schema.cloud/v1/schemas/",
  "type": "object",
  "title": "客户",
  "description": "客户信息",
  "properties": {
    "name": {
      "type": "string"
    },
    "birth": {
      "type": "string",
      "format": "date"
    },
    "company": {
      "$ref": "record",
      "$form": "company"
    }
  },
  "additionalProperties": false,
  "required": ["name","company"]
}
```

### 客户信息

使用 companyId：
```json
{
  "name": "Hansin",
  "company": "COMPANY_ID_HERE",
  "birth": "1997-01-15"
}
```

新建 company：
```json
{
  "name": "Hansin",
  "company": {
    "name": "广州金嗓音"
  },
  "birth": "1997-01-15"
}
```