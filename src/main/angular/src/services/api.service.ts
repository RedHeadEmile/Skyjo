//----------------------
// <auto-generated>
//     Generated using the NSwag toolchain v13.18.2.0 (NJsonSchema v10.8.0.0 (Newtonsoft.Json v11.0.0.0)) (http://NSwag.org)
// </auto-generated>
//----------------------

/* tslint:disable */
/* eslint-disable */
// ReSharper disable InconsistentNaming

import { mergeMap as _observableMergeMap, catchError as _observableCatch } from 'rxjs/operators';
import { Observable, throwError as _observableThrow, of as _observableOf } from 'rxjs';
import { Injectable, Inject, Optional, InjectionToken } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse, HttpResponseBase } from '@angular/common/http';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private http: HttpClient;
    private baseUrl: string;
    protected jsonParseReviver: ((key: string, value: any) => any) | undefined = undefined;

    constructor(@Inject(HttpClient) http: HttpClient, @Optional() @Inject(API_BASE_URL) baseUrl?: string) {
        this.http = http;
        this.baseUrl = baseUrl !== undefined && baseUrl !== null ? baseUrl : "http://localhost:8080";
    }

    /**
     * @return OK
     */
    showCurrentPlayer(): Observable<SkyjoPlayerViewModel> {
        let url_ = this.baseUrl + "/current-player";
        url_ = url_.replace(/[?&]$/, "");

        let options_ : any = {
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Accept": "*/*"
            })
        };

        return this.http.request("get", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processShowCurrentPlayer(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processShowCurrentPlayer(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<SkyjoPlayerViewModel>;
                }
            } else
                return _observableThrow(response_) as any as Observable<SkyjoPlayerViewModel>;
        }));
    }

    protected processShowCurrentPlayer(response: HttpResponseBase): Observable<SkyjoPlayerViewModel> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 200) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            let result200: any = null;
            let resultData200 = _responseText === "" ? null : JSON.parse(_responseText, this.jsonParseReviver);
            result200 = SkyjoPlayerViewModel.fromJS(resultData200);
            return _observableOf(result200);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return OK
     */
    updateCurrentPlayerSettings(body: SkyjoCurrentPlayerSettingsUpdateRequest): Observable<SkyjoPlayerViewModel> {
        let url_ = this.baseUrl + "/current-player";
        url_ = url_.replace(/[?&]$/, "");

        const content_ = JSON.stringify(body);

        let options_ : any = {
            body: content_,
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Content-Type": "application/json",
                "Accept": "*/*"
            })
        };

        return this.http.request("put", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processUpdateCurrentPlayerSettings(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processUpdateCurrentPlayerSettings(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<SkyjoPlayerViewModel>;
                }
            } else
                return _observableThrow(response_) as any as Observable<SkyjoPlayerViewModel>;
        }));
    }

    protected processUpdateCurrentPlayerSettings(response: HttpResponseBase): Observable<SkyjoPlayerViewModel> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 200) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            let result200: any = null;
            let resultData200 = _responseText === "" ? null : JSON.parse(_responseText, this.jsonParseReviver);
            result200 = SkyjoPlayerViewModel.fromJS(resultData200);
            return _observableOf(result200);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return OK
     */
    updateCurrentPlayerRoom(body: SkyjoCurrentPlayerRoomUpdateRequestViewModel): Observable<SkyjoRoomViewModel> {
        let url_ = this.baseUrl + "/current-player/room";
        url_ = url_.replace(/[?&]$/, "");

        const content_ = JSON.stringify(body);

        let options_ : any = {
            body: content_,
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Content-Type": "application/json",
                "Accept": "*/*"
            })
        };

        return this.http.request("put", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processUpdateCurrentPlayerRoom(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processUpdateCurrentPlayerRoom(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<SkyjoRoomViewModel>;
                }
            } else
                return _observableThrow(response_) as any as Observable<SkyjoRoomViewModel>;
        }));
    }

    protected processUpdateCurrentPlayerRoom(response: HttpResponseBase): Observable<SkyjoRoomViewModel> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 200) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            let result200: any = null;
            let resultData200 = _responseText === "" ? null : JSON.parse(_responseText, this.jsonParseReviver);
            result200 = SkyjoRoomViewModel.fromJS(resultData200);
            return _observableOf(result200);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return No Content
     */
    deleteCurrentPlayerRoom(): Observable<void> {
        let url_ = this.baseUrl + "/current-player/room";
        url_ = url_.replace(/[?&]$/, "");

        let options_ : any = {
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
            })
        };

        return this.http.request("delete", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processDeleteCurrentPlayerRoom(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processDeleteCurrentPlayerRoom(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<void>;
                }
            } else
                return _observableThrow(response_) as any as Observable<void>;
        }));
    }

    protected processDeleteCurrentPlayerRoom(response: HttpResponseBase): Observable<void> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return _observableOf(null as any);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return No Content
     */
    updateCurrentPlayerRoomDisplayName(body: SkyjoCurrentPlayerRoomDisplayNameRequestViewModel): Observable<void> {
        let url_ = this.baseUrl + "/current-player/room/display-name";
        url_ = url_.replace(/[?&]$/, "");

        const content_ = JSON.stringify(body);

        let options_ : any = {
            body: content_,
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Content-Type": "application/json",
            })
        };

        return this.http.request("put", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processUpdateCurrentPlayerRoomDisplayName(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processUpdateCurrentPlayerRoomDisplayName(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<void>;
                }
            } else
                return _observableThrow(response_) as any as Observable<void>;
        }));
    }

    protected processUpdateCurrentPlayerRoomDisplayName(response: HttpResponseBase): Observable<void> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return _observableOf(null as any);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return OK
     */
    indexRoom(): Observable<SkyjoRoomViewModel[]> {
        let url_ = this.baseUrl + "/rooms";
        url_ = url_.replace(/[?&]$/, "");

        let options_ : any = {
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Accept": "*/*"
            })
        };

        return this.http.request("get", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processIndexRoom(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processIndexRoom(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<SkyjoRoomViewModel[]>;
                }
            } else
                return _observableThrow(response_) as any as Observable<SkyjoRoomViewModel[]>;
        }));
    }

    protected processIndexRoom(response: HttpResponseBase): Observable<SkyjoRoomViewModel[]> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 200) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            let result200: any = null;
            let resultData200 = _responseText === "" ? null : JSON.parse(_responseText, this.jsonParseReviver);
            if (Array.isArray(resultData200)) {
                result200 = [] as any;
                for (let item of resultData200)
                    result200!.push(SkyjoRoomViewModel.fromJS(item));
            }
            else {
                result200 = <any>null;
            }
            return _observableOf(result200);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return OK
     */
    storeRoom(body: SkyjoRoomStoreRequestViewModel): Observable<SkyjoRoomViewModel> {
        let url_ = this.baseUrl + "/rooms";
        url_ = url_.replace(/[?&]$/, "");

        const content_ = JSON.stringify(body);

        let options_ : any = {
            body: content_,
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Content-Type": "application/json",
                "Accept": "*/*"
            })
        };

        return this.http.request("post", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processStoreRoom(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processStoreRoom(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<SkyjoRoomViewModel>;
                }
            } else
                return _observableThrow(response_) as any as Observable<SkyjoRoomViewModel>;
        }));
    }

    protected processStoreRoom(response: HttpResponseBase): Observable<SkyjoRoomViewModel> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 200) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            let result200: any = null;
            let resultData200 = _responseText === "" ? null : JSON.parse(_responseText, this.jsonParseReviver);
            result200 = SkyjoRoomViewModel.fromJS(resultData200);
            return _observableOf(result200);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return No Content
     */
    storeGameAction(body: SkyjoGameActionViewModel): Observable<void> {
        let url_ = this.baseUrl + "/game-action";
        url_ = url_.replace(/[?&]$/, "");

        const content_ = JSON.stringify(body);

        let options_ : any = {
            body: content_,
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Content-Type": "application/json",
            })
        };

        return this.http.request("post", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processStoreGameAction(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processStoreGameAction(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<void>;
                }
            } else
                return _observableThrow(response_) as any as Observable<void>;
        }));
    }

    protected processStoreGameAction(response: HttpResponseBase): Observable<void> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return _observableOf(null as any);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }

    /**
     * @return OK
     */
    showRoom(id: string): Observable<SkyjoRoomViewModel> {
        let url_ = this.baseUrl + "/rooms/{id}";
        if (id === undefined || id === null)
            throw new Error("The parameter 'id' must be defined.");
        url_ = url_.replace("{id}", encodeURIComponent("" + id));
        url_ = url_.replace(/[?&]$/, "");

        let options_ : any = {
            observe: "response",
            responseType: "blob",
            withCredentials: true,
            headers: new HttpHeaders({
                "Accept": "*/*"
            })
        };

        return this.http.request("get", url_, options_).pipe(_observableMergeMap((response_ : any) => {
            return this.processShowRoom(response_);
        })).pipe(_observableCatch((response_: any) => {
            if (response_ instanceof HttpResponseBase) {
                try {
                    return this.processShowRoom(response_ as any);
                } catch (e) {
                    return _observableThrow(e) as any as Observable<SkyjoRoomViewModel>;
                }
            } else
                return _observableThrow(response_) as any as Observable<SkyjoRoomViewModel>;
        }));
    }

    protected processShowRoom(response: HttpResponseBase): Observable<SkyjoRoomViewModel> {
        const status = response.status;
        const responseBlob =
            response instanceof HttpResponse ? response.body :
            (response as any).error instanceof Blob ? (response as any).error : undefined;

        let _headers: any = {}; if (response.headers) { for (let key of response.headers.keys()) { _headers[key] = response.headers.get(key); }}
        if (status === 200) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            let result200: any = null;
            let resultData200 = _responseText === "" ? null : JSON.parse(_responseText, this.jsonParseReviver);
            result200 = SkyjoRoomViewModel.fromJS(resultData200);
            return _observableOf(result200);
            }));
        } else if (status !== 200 && status !== 204) {
            return blobToText(responseBlob).pipe(_observableMergeMap((_responseText: string) => {
            return throwException("An unexpected server error occurred.", status, _responseText, _headers);
            }));
        }
        return _observableOf(null as any);
    }
}

export class SkyjoCurrentPlayerSettingsUpdateRequest implements ISkyjoCurrentPlayerSettingsUpdateRequest {
    displayName!: string;

    [key: string]: any;

    constructor(data?: ISkyjoCurrentPlayerSettingsUpdateRequest) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.displayName = _data["displayName"];
        }
    }

    static fromJS(data: any): SkyjoCurrentPlayerSettingsUpdateRequest {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoCurrentPlayerSettingsUpdateRequest();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["displayName"] = this.displayName;
        return data;
    }
}

export interface ISkyjoCurrentPlayerSettingsUpdateRequest {
    displayName: string;

    [key: string]: any;
}

export class SkyjoPlayerViewModel implements ISkyjoPlayerViewModel {
    id!: string;
    displayName!: string;

    [key: string]: any;

    constructor(data?: ISkyjoPlayerViewModel) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.id = _data["id"];
            this.displayName = _data["displayName"];
        }
    }

    static fromJS(data: any): SkyjoPlayerViewModel {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoPlayerViewModel();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["id"] = this.id;
        data["displayName"] = this.displayName;
        return data;
    }
}

export interface ISkyjoPlayerViewModel {
    id: string;
    displayName: string;

    [key: string]: any;
}

export class SkyjoCurrentPlayerRoomUpdateRequestViewModel implements ISkyjoCurrentPlayerRoomUpdateRequestViewModel {
    roomSecretCode!: string;

    [key: string]: any;

    constructor(data?: ISkyjoCurrentPlayerRoomUpdateRequestViewModel) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.roomSecretCode = _data["roomSecretCode"];
        }
    }

    static fromJS(data: any): SkyjoCurrentPlayerRoomUpdateRequestViewModel {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoCurrentPlayerRoomUpdateRequestViewModel();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["roomSecretCode"] = this.roomSecretCode;
        return data;
    }
}

export interface ISkyjoCurrentPlayerRoomUpdateRequestViewModel {
    roomSecretCode: string;

    [key: string]: any;
}

export class SkyjoRoomMemberViewModel implements ISkyjoRoomMemberViewModel {
    playerId!: string;
    playerDisplayName!: string;
    board!: number[];
    scores!: number[];

    [key: string]: any;

    constructor(data?: ISkyjoRoomMemberViewModel) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
        if (!data) {
            this.board = [];
            this.scores = [];
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.playerId = _data["playerId"];
            this.playerDisplayName = _data["playerDisplayName"];
            if (Array.isArray(_data["board"])) {
                this.board = [] as any;
                for (let item of _data["board"])
                    this.board!.push(item);
            }
            if (Array.isArray(_data["scores"])) {
                this.scores = [] as any;
                for (let item of _data["scores"])
                    this.scores!.push(item);
            }
        }
    }

    static fromJS(data: any): SkyjoRoomMemberViewModel {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoRoomMemberViewModel();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["playerId"] = this.playerId;
        data["playerDisplayName"] = this.playerDisplayName;
        if (Array.isArray(this.board)) {
            data["board"] = [];
            for (let item of this.board)
                data["board"].push(item);
        }
        if (Array.isArray(this.scores)) {
            data["scores"] = [];
            for (let item of this.scores)
                data["scores"].push(item);
        }
        return data;
    }
}

export interface ISkyjoRoomMemberViewModel {
    playerId: string;
    playerDisplayName: string;
    board: number[];
    scores: number[];

    [key: string]: any;
}

export class SkyjoRoomViewModel implements ISkyjoRoomViewModel {
    id!: string;
    displayName!: string;
    secretCode!: string;
    ownerId!: string;
    currentTurn!: number;
    currentTurnPlayerId?: string;
    currentTurnPlayerEndAt!: number;
    gameBeginAt!: number;
    status!: SkyjoRoomViewModelStatus;
    currentDrawnCard?: number;
    lastDiscardedCard?: number;
    members!: SkyjoRoomMemberViewModel[];

    [key: string]: any;

    constructor(data?: ISkyjoRoomViewModel) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
        if (!data) {
            this.members = [];
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.id = _data["id"];
            this.displayName = _data["displayName"];
            this.secretCode = _data["secretCode"];
            this.ownerId = _data["ownerId"];
            this.currentTurn = _data["currentTurn"];
            this.currentTurnPlayerId = _data["currentTurnPlayerId"];
            this.currentTurnPlayerEndAt = _data["currentTurnPlayerEndAt"];
            this.gameBeginAt = _data["gameBeginAt"];
            this.status = _data["status"];
            this.currentDrawnCard = _data["currentDrawnCard"];
            this.lastDiscardedCard = _data["lastDiscardedCard"];
            if (Array.isArray(_data["members"])) {
                this.members = [] as any;
                for (let item of _data["members"])
                    this.members!.push(SkyjoRoomMemberViewModel.fromJS(item));
            }
        }
    }

    static fromJS(data: any): SkyjoRoomViewModel {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoRoomViewModel();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["id"] = this.id;
        data["displayName"] = this.displayName;
        data["secretCode"] = this.secretCode;
        data["ownerId"] = this.ownerId;
        data["currentTurn"] = this.currentTurn;
        data["currentTurnPlayerId"] = this.currentTurnPlayerId;
        data["currentTurnPlayerEndAt"] = this.currentTurnPlayerEndAt;
        data["gameBeginAt"] = this.gameBeginAt;
        data["status"] = this.status;
        data["currentDrawnCard"] = this.currentDrawnCard;
        data["lastDiscardedCard"] = this.lastDiscardedCard;
        if (Array.isArray(this.members)) {
            data["members"] = [];
            for (let item of this.members)
                data["members"].push(item.toJSON());
        }
        return data;
    }
}

export interface ISkyjoRoomViewModel {
    id: string;
    displayName: string;
    secretCode: string;
    ownerId: string;
    currentTurn: number;
    currentTurnPlayerId?: string;
    currentTurnPlayerEndAt: number;
    gameBeginAt: number;
    status: SkyjoRoomViewModelStatus;
    currentDrawnCard?: number;
    lastDiscardedCard?: number;
    members: SkyjoRoomMemberViewModel[];

    [key: string]: any;
}

export class SkyjoCurrentPlayerRoomDisplayNameRequestViewModel implements ISkyjoCurrentPlayerRoomDisplayNameRequestViewModel {
    newDisplayName!: string;

    [key: string]: any;

    constructor(data?: ISkyjoCurrentPlayerRoomDisplayNameRequestViewModel) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.newDisplayName = _data["newDisplayName"];
        }
    }

    static fromJS(data: any): SkyjoCurrentPlayerRoomDisplayNameRequestViewModel {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoCurrentPlayerRoomDisplayNameRequestViewModel();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["newDisplayName"] = this.newDisplayName;
        return data;
    }
}

export interface ISkyjoCurrentPlayerRoomDisplayNameRequestViewModel {
    newDisplayName: string;

    [key: string]: any;
}

export class SkyjoRoomStoreRequestViewModel implements ISkyjoRoomStoreRequestViewModel {
    displayName!: string;

    [key: string]: any;

    constructor(data?: ISkyjoRoomStoreRequestViewModel) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.displayName = _data["displayName"];
        }
    }

    static fromJS(data: any): SkyjoRoomStoreRequestViewModel {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoRoomStoreRequestViewModel();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["displayName"] = this.displayName;
        return data;
    }
}

export interface ISkyjoRoomStoreRequestViewModel {
    displayName: string;

    [key: string]: any;
}

export class SkyjoGameActionViewModel implements ISkyjoGameActionViewModel {
    type!: SkyjoGameActionViewModelType;
    cardIndex?: number;

    [key: string]: any;

    constructor(data?: ISkyjoGameActionViewModel) {
        if (data) {
            for (var property in data) {
                if (data.hasOwnProperty(property))
                    (<any>this)[property] = (<any>data)[property];
            }
        }
    }

    init(_data?: any) {
        if (_data) {
            for (var property in _data) {
                if (_data.hasOwnProperty(property))
                    this[property] = _data[property];
            }
            this.type = _data["type"];
            this.cardIndex = _data["cardIndex"];
        }
    }

    static fromJS(data: any): SkyjoGameActionViewModel {
        data = typeof data === 'object' ? data : {};
        let result = new SkyjoGameActionViewModel();
        result.init(data);
        return result;
    }

    toJSON(data?: any) {
        data = typeof data === 'object' ? data : {};
        for (var property in this) {
            if (this.hasOwnProperty(property))
                data[property] = this[property];
        }
        data["type"] = this.type;
        data["cardIndex"] = this.cardIndex;
        return data;
    }
}

export interface ISkyjoGameActionViewModel {
    type: SkyjoGameActionViewModelType;
    cardIndex?: number;

    [key: string]: any;
}

export enum SkyjoRoomViewModelStatus {
    WAITING_FOR_PLAYERS = "WAITING_FOR_PLAYERS",
    SELECTING_CARDS_PHASE = "SELECTING_CARDS_PHASE",
    TURNS_IN_PROGRESS = "TURNS_IN_PROGRESS",
    FINISHED = "FINISHED",
}

export enum SkyjoGameActionViewModelType {
    PICK_A_CARD = "PICK_A_CARD",
    EXCHANGE_WITH_PICKED_CARD = "EXCHANGE_WITH_PICKED_CARD",
    IGNORE_PICKED_CARD = "IGNORE_PICKED_CARD",
    EXCHANGE_WITH_DISCARDED_CARD = "EXCHANGE_WITH_DISCARDED_CARD",
    FLIP_A_CARD = "FLIP_A_CARD",
}

export class ApiException extends Error {
    override message: string;
    status: number;
    response: string;
    headers: { [key: string]: any; };
    result: any;

    constructor(message: string, status: number, response: string, headers: { [key: string]: any; }, result: any) {
        super();

        this.message = message;
        this.status = status;
        this.response = response;
        this.headers = headers;
        this.result = result;
    }

    protected isApiException = true;

    static isApiException(obj: any): obj is ApiException {
        return obj.isApiException === true;
    }
}

function throwException(message: string, status: number, response: string, headers: { [key: string]: any; }, result?: any): Observable<any> {
    if (result !== null && result !== undefined)
        return _observableThrow(result);
    else
        return _observableThrow(new ApiException(message, status, response, headers, null));
}

function blobToText(blob: any): Observable<string> {
    return new Observable<string>((observer: any) => {
        if (!blob) {
            observer.next("");
            observer.complete();
        } else {
            let reader = new FileReader();
            reader.onload = event => {
                observer.next((event.target as any).result);
                observer.complete();
            };
            reader.readAsText(blob);
        }
    });
}