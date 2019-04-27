import { Moment } from 'moment';
import { ITag } from 'app/shared/model/tag.model';

export interface IEntry {
    id?: number;
    title?: string;
    content?: any;
    date?: Moment;
    blogName?: string;
    blogId?: number;
    tags?: ITag[];
}

export class Entry implements IEntry {
    constructor(
        public id?: number,
        public title?: string,
        public content?: any,
        public date?: Moment,
        public blogName?: string,
        public blogId?: number,
        public tags?: ITag[]
    ) {}
}
