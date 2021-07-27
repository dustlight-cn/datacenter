/**
 * 数据中心服务
 * 提供表单管理、表单记录增删改查等服务。
 *
 * The version of the OpenAPI document: v1
 * Contact: hansin@goodvoice.com
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { Configuration } from './configuration';
import { AxiosPromise, AxiosInstance } from 'axios';
import { RequestArgs, BaseAPI } from './base';
/**
 *
 * @export
 * @interface BetweenQuery
 */
export interface BetweenQuery {
    /**
     *
     * @type {string}
     * @memberof BetweenQuery
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof BetweenQuery
     */
    opt?: BetweenQueryOptEnum;
    /**
     *
     * @type {Rangeable}
     * @memberof BetweenQuery
     */
    value?: Rangeable;
}
/**
    * @export
    * @enum {string}
    */
export declare enum BetweenQueryOptEnum {
    Equal = "EQUAL",
    Match = "MATCH",
    In = "IN",
    Between = "BETWEEN"
}
/**
 *
 * @export
 * @interface BooleanItem
 */
export interface BooleanItem {
    /**
     *
     * @type {string}
     * @memberof BooleanItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof BooleanItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof BooleanItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof BooleanItem
     */
    type?: BooleanItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof BooleanItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof BooleanItem
     */
    required?: boolean;
}
/**
    * @export
    * @enum {string}
    */
export declare enum BooleanItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface DateItem
 */
export interface DateItem {
    /**
     *
     * @type {string}
     * @memberof DateItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof DateItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof DateItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof DateItem
     */
    type?: DateItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof DateItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof DateItem
     */
    required?: boolean;
    /**
     *
     * @type {RangeableInstant}
     * @memberof DateItem
     */
    dateRange?: RangeableInstant;
}
/**
    * @export
    * @enum {string}
    */
export declare enum DateItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface DoubleItem
 */
export interface DoubleItem {
    /**
     *
     * @type {string}
     * @memberof DoubleItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof DoubleItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof DoubleItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof DoubleItem
     */
    type?: DoubleItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof DoubleItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof DoubleItem
     */
    required?: boolean;
    /**
     *
     * @type {RangeableDouble}
     * @memberof DoubleItem
     */
    doubleRange?: RangeableDouble;
}
/**
    * @export
    * @enum {string}
    */
export declare enum DoubleItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface ElasticItem
 */
export interface ElasticItem {
    /**
     *
     * @type {string}
     * @memberof ElasticItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof ElasticItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof ElasticItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof ElasticItem
     */
    type?: ElasticItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof ElasticItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof ElasticItem
     */
    required?: boolean;
    /**
     *
     * @type {Array<Item>}
     * @memberof ElasticItem
     */
    options?: Array<Item>;
}
/**
    * @export
    * @enum {string}
    */
export declare enum ElasticItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface EqualQuery
 */
export interface EqualQuery {
    /**
     *
     * @type {string}
     * @memberof EqualQuery
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof EqualQuery
     */
    opt?: EqualQueryOptEnum;
    /**
     *
     * @type {object}
     * @memberof EqualQuery
     */
    value?: object;
}
/**
    * @export
    * @enum {string}
    */
export declare enum EqualQueryOptEnum {
    Equal = "EQUAL",
    Match = "MATCH",
    In = "IN",
    Between = "BETWEEN"
}
/**
 *
 * @export
 * @interface FileItem
 */
export interface FileItem {
    /**
     *
     * @type {string}
     * @memberof FileItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof FileItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof FileItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof FileItem
     */
    type?: FileItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof FileItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof FileItem
     */
    required?: boolean;
    /**
     *
     * @type {string}
     * @memberof FileItem
     */
    mime?: string;
}
/**
    * @export
    * @enum {string}
    */
export declare enum FileItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface Form
 */
export interface Form {
    /**
     *
     * @type {string}
     * @memberof Form
     */
    id?: string;
    /**
     *
     * @type {number}
     * @memberof Form
     */
    version?: number;
    /**
     *
     * @type {string}
     * @memberof Form
     */
    createdAt?: string;
    /**
     *
     * @type {string}
     * @memberof Form
     */
    clientId?: string;
    /**
     *
     * @type {string}
     * @memberof Form
     */
    owner?: string;
    /**
     *
     * @type {string}
     * @memberof Form
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof Form
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof Form
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof Form
     */
    primaryKey?: string;
    /**
     *
     * @type {Array<ItemGroup>}
     * @memberof Form
     */
    groups?: Array<ItemGroup>;
}
/**
 *
 * @export
 * @interface FormItem
 */
export interface FormItem {
    /**
     *
     * @type {string}
     * @memberof FormItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof FormItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof FormItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof FormItem
     */
    type?: FormItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof FormItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof FormItem
     */
    required?: boolean;
    /**
     *
     * @type {string}
     * @memberof FormItem
     */
    form?: string;
}
/**
    * @export
    * @enum {string}
    */
export declare enum FormItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface FormRecord
 */
export interface FormRecord {
    /**
     *
     * @type {string}
     * @memberof FormRecord
     */
    id?: string;
    /**
     *
     * @type {string}
     * @memberof FormRecord
     */
    clientId?: string;
    /**
     *
     * @type {string}
     * @memberof FormRecord
     */
    owner?: string;
    /**
     *
     * @type {string}
     * @memberof FormRecord
     */
    formId?: string;
    /**
     *
     * @type {string}
     * @memberof FormRecord
     */
    formName?: string;
    /**
     *
     * @type {number}
     * @memberof FormRecord
     */
    formVersion?: number;
    /**
     *
     * @type {string}
     * @memberof FormRecord
     */
    createdAt?: string;
    /**
     *
     * @type {string}
     * @memberof FormRecord
     */
    updatedAt?: string;
    /**
     *
     * @type {{ [key: string]: object; }}
     * @memberof FormRecord
     */
    data?: {
        [key: string]: object;
    };
}
/**
 *
 * @export
 * @interface InQuery
 */
export interface InQuery {
    /**
     *
     * @type {string}
     * @memberof InQuery
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof InQuery
     */
    opt?: InQueryOptEnum;
    /**
     *
     * @type {Array<object>}
     * @memberof InQuery
     */
    value?: Array<object>;
}
/**
    * @export
    * @enum {string}
    */
export declare enum InQueryOptEnum {
    Equal = "EQUAL",
    Match = "MATCH",
    In = "IN",
    Between = "BETWEEN"
}
/**
 *
 * @export
 * @interface IntItem
 */
export interface IntItem {
    /**
     *
     * @type {string}
     * @memberof IntItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof IntItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof IntItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof IntItem
     */
    type?: IntItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof IntItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof IntItem
     */
    required?: boolean;
    /**
     *
     * @type {RangeableInteger}
     * @memberof IntItem
     */
    intRange?: RangeableInteger;
}
/**
    * @export
    * @enum {string}
    */
export declare enum IntItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 * @type Item
 * @export
 */
export declare type Item = BooleanItem | DateItem | DoubleItem | ElasticItem | FileItem | FormItem | IntItem | SelectItem | StringItem | UserItem;
/**
 *
 * @export
 * @interface ItemGroup
 */
export interface ItemGroup {
    /**
     *
     * @type {string}
     * @memberof ItemGroup
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof ItemGroup
     */
    description?: string;
    /**
     *
     * @type {Array<Item>}
     * @memberof ItemGroup
     */
    items?: Array<Item>;
}
/**
 *
 * @export
 * @interface MatchQuery
 */
export interface MatchQuery {
    /**
     *
     * @type {string}
     * @memberof MatchQuery
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof MatchQuery
     */
    opt?: MatchQueryOptEnum;
    /**
     *
     * @type {string}
     * @memberof MatchQuery
     */
    value?: string;
}
/**
    * @export
    * @enum {string}
    */
export declare enum MatchQueryOptEnum {
    Equal = "EQUAL",
    Match = "MATCH",
    In = "IN",
    Between = "BETWEEN"
}
/**
 * @type QueryObject
 * @export
 */
export declare type QueryObject = BetweenQuery | EqualQuery | InQuery | MatchQuery;
/**
 *
 * @export
 * @interface QueryResultForm
 */
export interface QueryResultForm {
    /**
     *
     * @type {number}
     * @memberof QueryResultForm
     */
    count?: number;
    /**
     *
     * @type {Array<Form>}
     * @memberof QueryResultForm
     */
    data?: Array<Form>;
}
/**
 *
 * @export
 * @interface QueryResultFormRecord
 */
export interface QueryResultFormRecord {
    /**
     *
     * @type {number}
     * @memberof QueryResultFormRecord
     */
    count?: number;
    /**
     *
     * @type {Array<FormRecord>}
     * @memberof QueryResultFormRecord
     */
    data?: Array<FormRecord>;
}
/**
 *
 * @export
 * @interface Rangeable
 */
export interface Rangeable {
    /**
     *
     * @type {object}
     * @memberof Rangeable
     */
    min?: object;
    /**
     *
     * @type {object}
     * @memberof Rangeable
     */
    max?: object;
    /**
     *
     * @type {boolean}
     * @memberof Rangeable
     */
    openInterval?: boolean;
}
/**
 *
 * @export
 * @interface RangeableDouble
 */
export interface RangeableDouble {
    /**
     *
     * @type {number}
     * @memberof RangeableDouble
     */
    min?: number;
    /**
     *
     * @type {number}
     * @memberof RangeableDouble
     */
    max?: number;
    /**
     *
     * @type {boolean}
     * @memberof RangeableDouble
     */
    openInterval?: boolean;
}
/**
 *
 * @export
 * @interface RangeableInstant
 */
export interface RangeableInstant {
    /**
     *
     * @type {string}
     * @memberof RangeableInstant
     */
    min?: string;
    /**
     *
     * @type {string}
     * @memberof RangeableInstant
     */
    max?: string;
    /**
     *
     * @type {boolean}
     * @memberof RangeableInstant
     */
    openInterval?: boolean;
}
/**
 *
 * @export
 * @interface RangeableInteger
 */
export interface RangeableInteger {
    /**
     *
     * @type {number}
     * @memberof RangeableInteger
     */
    min?: number;
    /**
     *
     * @type {number}
     * @memberof RangeableInteger
     */
    max?: number;
    /**
     *
     * @type {boolean}
     * @memberof RangeableInteger
     */
    openInterval?: boolean;
}
/**
 *
 * @export
 * @interface SelectItem
 */
export interface SelectItem {
    /**
     *
     * @type {string}
     * @memberof SelectItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof SelectItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof SelectItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof SelectItem
     */
    type?: SelectItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof SelectItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof SelectItem
     */
    required?: boolean;
    /**
     *
     * @type {RangeableInteger}
     * @memberof SelectItem
     */
    selectedRange?: RangeableInteger;
    /**
     *
     * @type {{ [key: string]: string; }}
     * @memberof SelectItem
     */
    options?: {
        [key: string]: string;
    };
}
/**
    * @export
    * @enum {string}
    */
export declare enum SelectItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface StringItem
 */
export interface StringItem {
    /**
     *
     * @type {string}
     * @memberof StringItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof StringItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof StringItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof StringItem
     */
    type?: StringItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof StringItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof StringItem
     */
    required?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof StringItem
     */
    multiline?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof StringItem
     */
    html?: boolean;
    /**
     *
     * @type {string}
     * @memberof StringItem
     */
    regex?: string;
}
/**
    * @export
    * @enum {string}
    */
export declare enum StringItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 *
 * @export
 * @interface UserItem
 */
export interface UserItem {
    /**
     *
     * @type {string}
     * @memberof UserItem
     */
    name?: string;
    /**
     *
     * @type {string}
     * @memberof UserItem
     */
    label?: string;
    /**
     *
     * @type {string}
     * @memberof UserItem
     */
    description?: string;
    /**
     *
     * @type {string}
     * @memberof UserItem
     */
    type?: UserItemTypeEnum;
    /**
     *
     * @type {boolean}
     * @memberof UserItem
     */
    array?: boolean;
    /**
     *
     * @type {boolean}
     * @memberof UserItem
     */
    required?: boolean;
}
/**
    * @export
    * @enum {string}
    */
export declare enum UserItemTypeEnum {
    Int = "INT",
    Double = "DOUBLE",
    String = "STRING",
    Date = "DATE",
    Boolean = "BOOLEAN",
    Form = "FORM",
    File = "FILE",
    Select = "SELECT",
    User = "USER",
    Elastic = "ELASTIC"
}
/**
 * FormsApi - axios parameter creator
 * @export
 */
export declare const FormsApiAxiosParamCreator: (configuration?: Configuration) => {
    /**
     * 创建一个表单，返回创建后的表单。
     * @summary 创建表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createForm: (form: Form, options?: any) => Promise<RequestArgs>;
    /**
     * 通过名称删除所有表单。
     * @summary 删除表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deleteForm: (name: string, options?: any) => Promise<RequestArgs>;
    /**
     * 通过 ID 获取表单结构。
     * @summary 获取表单
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getFormById: (id: string, options?: any) => Promise<RequestArgs>;
    /**
     * 当 query 不为空时，不分版本搜索表单，提供 name 可以限制搜索范围。当 query 为空时，若 name 不为空则列出该名称表单的所有版本，否则列出此应用的所有最新表单结构。
     * @summary 查询或列出表单
     * @param {string} [name]
     * @param {string} [query]
     * @param {number} [page]
     * @param {number} [size]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getForms: (name?: string, query?: string, page?: number, size?: number, options?: any) => Promise<RequestArgs>;
    /**
     * 通过名称获取最新版本的表单。
     * @summary 获取最新的表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getLatestForm: (name: string, options?: any) => Promise<RequestArgs>;
    /**
     * 通过名称更新表单结构。
     * @summary 更新表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updateForm: (form: Form, options?: any) => Promise<RequestArgs>;
};
/**
 * FormsApi - functional programming interface
 * @export
 */
export declare const FormsApiFp: (configuration?: Configuration) => {
    /**
     * 创建一个表单，返回创建后的表单。
     * @summary 创建表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createForm(form: Form, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Form>>;
    /**
     * 通过名称删除所有表单。
     * @summary 删除表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deleteForm(name: string, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>>;
    /**
     * 通过 ID 获取表单结构。
     * @summary 获取表单
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getFormById(id: string, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Form>>;
    /**
     * 当 query 不为空时，不分版本搜索表单，提供 name 可以限制搜索范围。当 query 为空时，若 name 不为空则列出该名称表单的所有版本，否则列出此应用的所有最新表单结构。
     * @summary 查询或列出表单
     * @param {string} [name]
     * @param {string} [query]
     * @param {number} [page]
     * @param {number} [size]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getForms(name?: string, query?: string, page?: number, size?: number, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<QueryResultForm>>;
    /**
     * 通过名称获取最新版本的表单。
     * @summary 获取最新的表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getLatestForm(name: string, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Form>>;
    /**
     * 通过名称更新表单结构。
     * @summary 更新表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updateForm(form: Form, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Form>>;
};
/**
 * FormsApi - factory interface
 * @export
 */
export declare const FormsApiFactory: (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) => {
    /**
     * 创建一个表单，返回创建后的表单。
     * @summary 创建表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createForm(form: Form, options?: any): AxiosPromise<Form>;
    /**
     * 通过名称删除所有表单。
     * @summary 删除表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deleteForm(name: string, options?: any): AxiosPromise<void>;
    /**
     * 通过 ID 获取表单结构。
     * @summary 获取表单
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getFormById(id: string, options?: any): AxiosPromise<Form>;
    /**
     * 当 query 不为空时，不分版本搜索表单，提供 name 可以限制搜索范围。当 query 为空时，若 name 不为空则列出该名称表单的所有版本，否则列出此应用的所有最新表单结构。
     * @summary 查询或列出表单
     * @param {string} [name]
     * @param {string} [query]
     * @param {number} [page]
     * @param {number} [size]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getForms(name?: string, query?: string, page?: number, size?: number, options?: any): AxiosPromise<QueryResultForm>;
    /**
     * 通过名称获取最新版本的表单。
     * @summary 获取最新的表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getLatestForm(name: string, options?: any): AxiosPromise<Form>;
    /**
     * 通过名称更新表单结构。
     * @summary 更新表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updateForm(form: Form, options?: any): AxiosPromise<Form>;
};
/**
 * FormsApi - object-oriented interface
 * @export
 * @class FormsApi
 * @extends {BaseAPI}
 */
export declare class FormsApi extends BaseAPI {
    /**
     * 创建一个表单，返回创建后的表单。
     * @summary 创建表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof FormsApi
     */
    createForm(form: Form, options?: any): Promise<import("axios").AxiosResponse<Form>>;
    /**
     * 通过名称删除所有表单。
     * @summary 删除表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof FormsApi
     */
    deleteForm(name: string, options?: any): Promise<import("axios").AxiosResponse<void>>;
    /**
     * 通过 ID 获取表单结构。
     * @summary 获取表单
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof FormsApi
     */
    getFormById(id: string, options?: any): Promise<import("axios").AxiosResponse<Form>>;
    /**
     * 当 query 不为空时，不分版本搜索表单，提供 name 可以限制搜索范围。当 query 为空时，若 name 不为空则列出该名称表单的所有版本，否则列出此应用的所有最新表单结构。
     * @summary 查询或列出表单
     * @param {string} [name]
     * @param {string} [query]
     * @param {number} [page]
     * @param {number} [size]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof FormsApi
     */
    getForms(name?: string, query?: string, page?: number, size?: number, options?: any): Promise<import("axios").AxiosResponse<QueryResultForm>>;
    /**
     * 通过名称获取最新版本的表单。
     * @summary 获取最新的表单
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof FormsApi
     */
    getLatestForm(name: string, options?: any): Promise<import("axios").AxiosResponse<Form>>;
    /**
     * 通过名称更新表单结构。
     * @summary 更新表单
     * @param {Form} form
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof FormsApi
     */
    updateForm(form: Form, options?: any): Promise<import("axios").AxiosResponse<Form>>;
}
/**
 * RecordsApi - axios parameter creator
 * @export
 */
export declare const RecordsApiAxiosParamCreator: (configuration?: Configuration) => {
    /**
     * 提交一条表单记录。
     * @summary 创建表单记录
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createRecord: (formRecord: FormRecord, options?: any) => Promise<RequestArgs>;
    /**
     * 删除一条表单记录。
     * @summary 删除表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deleteRecord: (id: string, options?: any) => Promise<RequestArgs>;
    /**
     * 列出或搜索表单记录。
     * @summary 检索表单记录
     * @param {string} name 表单名称。
     * @param {string} [query] 关键词，对表单的 STRING 类型进行全文搜索。
     * @param {Array<string>} [orders] 排序字段，如：update （正序排序） -update（倒序排序）。
     * @param {number} [page]
     * @param {number} [size]
     * @param {Array<QueryObject>} [queryObject]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    findRecords: (name: string, query?: string, orders?: Array<string>, page?: number, size?: number, queryObject?: Array<QueryObject>, options?: any) => Promise<RequestArgs>;
    /**
     * 获取一条表单记录。
     * @summary 获取表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getRecord: (id: string, options?: any) => Promise<RequestArgs>;
    /**
     * 更新一条表单记录。
     * @summary 更新表单记录
     * @param {string} id
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updateRecord: (id: string, formRecord: FormRecord, options?: any) => Promise<RequestArgs>;
};
/**
 * RecordsApi - functional programming interface
 * @export
 */
export declare const RecordsApiFp: (configuration?: Configuration) => {
    /**
     * 提交一条表单记录。
     * @summary 创建表单记录
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createRecord(formRecord: FormRecord, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<FormRecord>>;
    /**
     * 删除一条表单记录。
     * @summary 删除表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deleteRecord(id: string, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>>;
    /**
     * 列出或搜索表单记录。
     * @summary 检索表单记录
     * @param {string} name 表单名称。
     * @param {string} [query] 关键词，对表单的 STRING 类型进行全文搜索。
     * @param {Array<string>} [orders] 排序字段，如：update （正序排序） -update（倒序排序）。
     * @param {number} [page]
     * @param {number} [size]
     * @param {Array<QueryObject>} [queryObject]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    findRecords(name: string, query?: string, orders?: Array<string>, page?: number, size?: number, queryObject?: Array<QueryObject>, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<QueryResultFormRecord>>;
    /**
     * 获取一条表单记录。
     * @summary 获取表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getRecord(id: string, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<FormRecord>>;
    /**
     * 更新一条表单记录。
     * @summary 更新表单记录
     * @param {string} id
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updateRecord(id: string, formRecord: FormRecord, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<FormRecord>>;
};
/**
 * RecordsApi - factory interface
 * @export
 */
export declare const RecordsApiFactory: (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) => {
    /**
     * 提交一条表单记录。
     * @summary 创建表单记录
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createRecord(formRecord: FormRecord, options?: any): AxiosPromise<FormRecord>;
    /**
     * 删除一条表单记录。
     * @summary 删除表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deleteRecord(id: string, options?: any): AxiosPromise<void>;
    /**
     * 列出或搜索表单记录。
     * @summary 检索表单记录
     * @param {string} name 表单名称。
     * @param {string} [query] 关键词，对表单的 STRING 类型进行全文搜索。
     * @param {Array<string>} [orders] 排序字段，如：update （正序排序） -update（倒序排序）。
     * @param {number} [page]
     * @param {number} [size]
     * @param {Array<QueryObject>} [queryObject]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    findRecords(name: string, query?: string, orders?: Array<string>, page?: number, size?: number, queryObject?: Array<QueryObject>, options?: any): AxiosPromise<QueryResultFormRecord>;
    /**
     * 获取一条表单记录。
     * @summary 获取表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getRecord(id: string, options?: any): AxiosPromise<FormRecord>;
    /**
     * 更新一条表单记录。
     * @summary 更新表单记录
     * @param {string} id
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updateRecord(id: string, formRecord: FormRecord, options?: any): AxiosPromise<FormRecord>;
};
/**
 * RecordsApi - object-oriented interface
 * @export
 * @class RecordsApi
 * @extends {BaseAPI}
 */
export declare class RecordsApi extends BaseAPI {
    /**
     * 提交一条表单记录。
     * @summary 创建表单记录
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof RecordsApi
     */
    createRecord(formRecord: FormRecord, options?: any): Promise<import("axios").AxiosResponse<FormRecord>>;
    /**
     * 删除一条表单记录。
     * @summary 删除表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof RecordsApi
     */
    deleteRecord(id: string, options?: any): Promise<import("axios").AxiosResponse<void>>;
    /**
     * 列出或搜索表单记录。
     * @summary 检索表单记录
     * @param {string} name 表单名称。
     * @param {string} [query] 关键词，对表单的 STRING 类型进行全文搜索。
     * @param {Array<string>} [orders] 排序字段，如：update （正序排序） -update（倒序排序）。
     * @param {number} [page]
     * @param {number} [size]
     * @param {Array<QueryObject>} [queryObject]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof RecordsApi
     */
    findRecords(name: string, query?: string, orders?: Array<string>, page?: number, size?: number, queryObject?: Array<QueryObject>, options?: any): Promise<import("axios").AxiosResponse<QueryResultFormRecord>>;
    /**
     * 获取一条表单记录。
     * @summary 获取表单记录
     * @param {string} id
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof RecordsApi
     */
    getRecord(id: string, options?: any): Promise<import("axios").AxiosResponse<FormRecord>>;
    /**
     * 更新一条表单记录。
     * @summary 更新表单记录
     * @param {string} id
     * @param {FormRecord} formRecord
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof RecordsApi
     */
    updateRecord(id: string, formRecord: FormRecord, options?: any): Promise<import("axios").AxiosResponse<FormRecord>>;
}
