import {Injectable} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Observable} from "rxjs/Observable";
import {LoginResponseParserService} from "./login-response-parser.service";
import {AuthCode} from "./auth-code";

@Injectable()
export class CodeProviderService {

  constructor(
    private route: ActivatedRoute,
    private loginResponseParser: LoginResponseParserService
  ) { }

  public get code(): Observable<AuthCode> {
    return this.route.fragment.map(
      fragment => this.loginResponseParser.parse(fragment)
    );
  }
}
