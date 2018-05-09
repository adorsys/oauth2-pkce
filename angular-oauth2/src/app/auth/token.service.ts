import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {environment} from "../../environments/environment";

@Injectable()
export class TokenService {

  constructor(private http: HttpClient) { }

  public getToken(code: string): Observable<void> {
    let url = `${environment.backendUrl}${environment.loginEndpoint}?code=${code}`;
    console.log(`GET ${url}`);

    return this.http.get<void>(
      url);
  }
}
