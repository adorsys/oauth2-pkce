import {Injectable} from '@angular/core';
import {User} from "./user";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/map';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class UserService {

  constructor(private http: HttpClient) {
  }

  public getUser(): Observable<User> {
    console.log("getting user info...");

    let principalResponse = this.http.get<PrincipalResponse>("/user");

    return principalResponse.map(r => {
      return {
        name: r.userAuthentication.details.name
      }
    });
  }
}

interface PrincipalResponse {
  "authorities": [
    {
      "authority": string
    }
    ],
  "details": {
    "remoteAddress": string,
    "sessionId": string,
    "tokenValue": string,
    "tokenType": "bearer",
    "decodedDetails"?: any
  },
  "authenticated": boolean,
  "userAuthentication": {
    "authorities": [
      {
        "authority": string
      }
      ],
    "details": {
      "sub": string,
      "name": string,
      "preferred_username": string,
      "given_name": string,
      "family_name": string,
      "email": string
    },
    "authenticated": boolean,
    "principal": string,
    "credentials": string,
    "name": string
  },
  "principal": string,
  "oauth2Request": {
    "clientId": string,
    "scope": [any],
    "requestParameters"?: any,
    "resourceIds": [any],
    "authorities": [any],
    "approved": boolean,
    "refresh": boolean,
    "redirectUri": any,
    "responseTypes": [any],
    "extensions": any,
    "grantType"?: any,
    "refreshTokenRequest"?: any
  },
  "clientOnly": boolean,
  "credentials": string,
  "name": string
}
