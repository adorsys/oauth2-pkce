import {Injectable} from '@angular/core';
import {AuthCode} from "./auth-code";

@Injectable()
export class LoginResponseParserService {
  private emptyAuthCode = new AuthCode();
  private codeRegex = /(?:^|\s)(.*&)?code=(.*?)(&.*)?(?:\s|$)/g;

  constructor() { }

  public parse(parameters: string): AuthCode {
    if(parameters == null) {
      return this.emptyAuthCode;
    }

    let match = this.codeRegex.exec(parameters);

    if(match == null) {
      return this.emptyAuthCode;
    }

    return new AuthCode(match[2]);
  }
}
