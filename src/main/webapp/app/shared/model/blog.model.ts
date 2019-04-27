export interface IBlog {
    id?: number;
    name?: string;
    handle?: string;
    userLogin?: string;
    userId?: number;
}

export class Blog implements IBlog {
    constructor(public id?: number, public name?: string, public handle?: string, public userLogin?: string, public userId?: number) {}
}
