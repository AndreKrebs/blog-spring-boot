import { Component, OnInit, ElementRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiDataUtils } from 'ng-jhipster';
import { ITag } from 'app/shared/model/tag.model';
import { TagService } from './tag.service';

@Component({
    selector: 'jhi-tag-update',
    templateUrl: './tag-update.component.html'
})
export class TagUpdateComponent implements OnInit {
    tag: ITag;
    isSaving: boolean;

    constructor(
        protected dataUtils: JhiDataUtils,
        protected tagService: TagService,
        protected elementRef: ElementRef,
        protected activatedRoute: ActivatedRoute
    ) {}

    ngOnInit() {
        this.isSaving = false;
        this.activatedRoute.data.subscribe(({ tag }) => {
            this.tag = tag;
        });
    }

    byteSize(field) {
        return this.dataUtils.byteSize(field);
    }

    openFile(contentType, field) {
        return this.dataUtils.openFile(contentType, field);
    }

    setFileData(event, entity, field, isImage) {
        this.dataUtils.setFileData(event, entity, field, isImage);
    }

    clearInputImage(field: string, fieldContentType: string, idInput: string) {
        this.dataUtils.clearInputImage(this.tag, this.elementRef, field, fieldContentType, idInput);
    }

    previousState() {
        window.history.back();
    }

    save() {
        this.isSaving = true;
        if (this.tag.id !== undefined) {
            this.subscribeToSaveResponse(this.tagService.update(this.tag));
        } else {
            this.subscribeToSaveResponse(this.tagService.create(this.tag));
        }
    }

    protected subscribeToSaveResponse(result: Observable<HttpResponse<ITag>>) {
        result.subscribe((res: HttpResponse<ITag>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
    }

    protected onSaveSuccess() {
        this.isSaving = false;
        this.previousState();
    }

    protected onSaveError() {
        this.isSaving = false;
    }
}
