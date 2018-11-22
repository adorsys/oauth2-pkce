import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Rx";
import {AppConfigService} from "../app.config.service";

@Injectable()
export class TokenService {

  constructor(
    private appConfigService: AppConfigService,
    private http: HttpClient
  ) { }

  public getToken(code: string): Observable<void> {
    let url = `${this.appConfigService.getBackendUrl()}${this.appConfigService.getTokenEndpoint()}?code=${code}&redirect_uri=${this.appConfigService.getRedirectUri()}`;
    return this.http.get<void>(url);
  }
}
