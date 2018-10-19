import {Injectable} from '@angular/core';
import {User} from "./user";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/map';
import {HttpClient} from "@angular/common/http";
import {AppConfigService} from "../app.config.service";

@Injectable()
export class UserService {

  constructor(private appConfigService: AppConfigService, private http: HttpClient) {
  }

  public getUser(): Observable<User> {
    const options = {
      withCredentials: true
    };

    const principalResponse = this.http.get<PrincipalResponse>(
      `${this.appConfigService.getBackendUrl()}/user`,
      options
    );

    return principalResponse.map(r => {
      return {
        name: r.name
      }
    });
  }
}

interface PrincipalResponse {
  authorities: any[],
  details: any,
  authenticated: boolean,
  principal: string,
  credentials: {
    given_name: string,
    family_name: string,
    name: string,
    sub: string,
    email: string,
    email_verified: boolean,
    person_id: any
  },
  name: string
}
