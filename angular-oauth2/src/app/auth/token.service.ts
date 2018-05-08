import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {TokenResponse} from "./token-response";
import {Observable} from "rxjs/Observable";
import {environment} from "../../environments/environment";

@Injectable()
export class TokenService {

  constructor(private http: HttpClient) { }

  public getToken(code: string): Observable<void> {
    return this.http.post<void>(
      `${environment.backendUrl}${environment.tokenEndpoint}`,
      {
        code: code
      }, {
        withCredentials: true
      });
  }
}
