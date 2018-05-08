import { TestBed, inject } from '@angular/core/testing';

import { LoginResponseParserService } from './login-response-parser.service';

describe('LoginResponseParserService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoginResponseParserService]
    });
  });

  it('should be created', inject([LoginResponseParserService], (service: LoginResponseParserService) => {
    expect(service).toBeTruthy();
  }));

  it('should parse null into empty AuthCode', inject([LoginResponseParserService], (service: LoginResponseParserService) => {
    expect(service.parse(null).isEmpty).toEqual(true);
  }));

  it('should not parse empty', inject([LoginResponseParserService], (service: LoginResponseParserService) => {
    expect(service.parse("").isEmpty).toEqual(true);
  }));

  it('should parse code only', inject([LoginResponseParserService], (service: LoginResponseParserService) => {
    let authCode = service.parse("code=abcABC123");

    expect(authCode.isEmpty).toEqual(false);
    expect(authCode.value).toEqual("abcABC123");
  }));

  it('should parse code behind other param', inject([LoginResponseParserService], (service: LoginResponseParserService) => {
    let authCode = service.parse("otherParam=xyz&code=abcABC123");

    expect(authCode.isEmpty).toEqual(false);
    expect(authCode.value).toEqual("abcABC123");
  }));

  it('should parse code before other param', inject([LoginResponseParserService], (service: LoginResponseParserService) => {
    let authCode = service.parse("code=abcABC123&otherParam=xyz");

    expect(authCode.isEmpty).toEqual(false);
    expect(authCode.value).toEqual("abcABC123");
  }));

  it('should parse code between other params', inject([LoginResponseParserService], (service: LoginResponseParserService) => {
    let authCode = service.parse("otherParam=xyz&code=abcABC123&otherParam1=xyz");

    expect(authCode.isEmpty).toEqual(false);
    expect(authCode.value).toEqual("abcABC123");
  }));

});
