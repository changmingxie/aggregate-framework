export interface searchData {
    domain: string | number;
    pageNum: number;
    row:string;
    isDeleted:boolean;
}

export interface resetData {
    domain: string | number;
    row: string | number;
    keys: Array<string | number>;
    // row: string;
}

export interface addData {
    database: string | number;
    password: string | number;
    port: string | number;
    domain: string | number;
    host: string | number;
    application: string;
    owners: Array<Owner>;
}

export interface Owner {
    name: string;
}
