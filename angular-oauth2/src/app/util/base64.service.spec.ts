import { TestBed, inject } from '@angular/core/testing';

import { Base64 } from './base64.service';

describe('Base64Service', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [Base64]
    });
  });

  it('should be created', inject([Base64], (service: Base64) => {
    expect(service).toBeTruthy();
  }));

  it('should encode', inject([Base64], (service: Base64) => {
    expect(service.encode("password")).toEqual("cGFzc3dvcmQ=");
  }));

  it('should decode', inject([Base64], (service: Base64) => {
    expect(service.decode("cGFzc3dvcmQ=")).toEqual("password");
  }));
});
