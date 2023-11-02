//
// Created by Outis on 2023/11/2.
//

#include <cstring>
#include <cstdlib>

#ifdef __cplusplus
extern "C" {
#endif

const char *cacert = "-----BEGIN CERTIFICATE-----\n"
                     "MIIDXTCCAkWgAwIBAgIJAPIkTTofA50zMA0GCSqGSIb3DQEBCwUAMEQxCzAJBgNV\n"
                     "BAYTAkNOMREwDwYDVQQIDAhzaGVuemhlbjERMA8GA1UECgwIc2t5d29ydGgxDzAN\n"
                     "BgNVBAMMBlJvb3RDQTAgFw0yMDA5MjQwMjAzMzVaGA8yMTIwMDgzMTAyMDMzNVow\n"
                     "RDELMAkGA1UEBhMCQ04xETAPBgNVBAgMCHNoZW56aGVuMREwDwYDVQQKDAhza3l3\n"
                     "b3J0aDEPMA0GA1UEAwwGUm9vdENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n"
                     "CgKCAQEA4K6JRE6pE8aoey+8HL4pFno84bIJHZo3Gfg0SUlqinOc6vgWzHaHBV5+\n"
                     "/RiQuvy2OCFFhunSk6XD2zdyHLn1b/KlKAyA6+6GEw1h1G+Tt4K4PJOnglChMlcQ\n"
                     "dUllOzLRhoaCxvWkh7fa62EAziLhJ+hHdSIgplOzz91IL/Dg/KaxsctzKi4WhE0W\n"
                     "5ilnhnSbtGU4aWlEV4jI3wMCJA2QY5746sOFYkP/OiL713IKLDzUw+RqmjPIvpe/\n"
                     "2ZKXwec2d0LEOmkiyVcIERBhocbs6/MtTCBMZR1zLCgaHxwBXiERF0uElba91Ip9\n"
                     "9Pzx7A9rJlhySvR9xV+dIn/i5DcluQIDAQABo1AwTjAdBgNVHQ4EFgQUwfDCgMKw\n"
                     "v2ZqphGAR8GuYyiGsvgwHwYDVR0jBBgwFoAUwfDCgMKwv2ZqphGAR8GuYyiGsvgw\n"
                     "DAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAKAxgyN93HedvMw0DjkJO\n"
                     "mmi0SUypMe2zsKlYT1yzucV5Phc0R/LOZR8Tqb7OfUE0SiGlFaXDD8G0KNNlOYY0\n"
                     "7Sz0YfC9SNU3IUq+DpO/dXgUUE3xiYofuGkyPxN+RF0JFxSjkewxxEXZzc6jDa1d\n"
                     "c1Rq3jrqwQ8/4pPlUg2nCmE3zzaFzfdUyK6vVKGegizetx8MNN/L3MItWCPiIwmt\n"
                     "4rfJ/UUfL2DXqe9KXFotV4jXRsyzbA7CjKp7dPsGJ/v3IIegSyNjHSsSgWTiEt/L\n"
                     "C3qUJp6giF5yWN1Njs0Q/x0FyMfu6zFfP4fmSQnQGXgEa8Sw+O+y+Yf6vu1RoTO3\n"
                     "Yw==\n"
                     "-----END CERTIFICATE-----\n";

const char *crtstr = "-----BEGIN CERTIFICATE-----\n"
                     "MIIDgDCCAmigAwIBAgIBAzANBgkqhkiG9w0BAQsFADBEMQswCQYDVQQGEwJDTjER\n"
                     "MA8GA1UECAwIc2hlbnpoZW4xETAPBgNVBAoMCHNreXdvcnRoMQ8wDQYDVQQDDAZS\n"
                     "b290Q0EwIBcNMjAwOTI0MDIyOTUyWhgPMjEyMDA4MzEwMjI5NTJaMEQxCzAJBgNV\n"
                     "BAYTAkNOMREwDwYDVQQIDAhzaGVuemhlbjERMA8GA1UECgwIc2t5d29ydGgxDzAN\n"
                     "BgNVBAMMBkNsaWVudDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMLh\n"
                     "rh/5C6qxldoAHWlyVwTa153DLylYwN8NiReS17L8KbqjcodjLDnSJMlfFMg+wygw\n"
                     "HJ28bSr34wnVeYFrLqMVpVZSJMbpD238avJEN43gmvwuUby6nykxWoRlTnAN5su+\n"
                     "VBgzEIw6i2hzftUQoDuQJm61S1vAQHHqXvy/RqFS+lF4/1NrECAkglX41Fwbu19M\n"
                     "ot5SriV/mHyhlXX76xryOI38a7GI8Ickc27nXnia7ZEMmobjNkZKgwzfGINExcz+\n"
                     "F2vTu2yQpBeoEJzCDIzt81lN9rl/qE+nzxx/Qdy1CqtCPZkR4JU7QZpEyz8gmdFk\n"
                     "NtJfwFPcrW9hRFLShwUCAwEAAaN7MHkwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0E\n"
                     "HxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFIS1nyER\n"
                     "5nNt5TM5/iWTTN+SKXVRMB8GA1UdIwQYMBaAFMHwwoDCsL9maqYRgEfBrmMohrL4\n"
                     "MA0GCSqGSIb3DQEBCwUAA4IBAQBptiAr3ReQ/OniaeHOCz4UX9wKn/oJy9nz+X4a\n"
                     "kNg/K+RtaNS29VhcrQndrh5CusJHl2fLo9+UXdDMA7Tuwnndsk3hpHTUItewtxiy\n"
                     "k3QmXFgZTV7JNoby+1FiLGT6y8LrkilRe5zA7wPReThCZrf4Dk5wnaPCohGyK7UI\n"
                     "8G7Z0gwMAYB7+DjbaE4tsiIZsCgnisBL4YH+oRH9GPva7O9Nu3i9a/1Vc4dIs01d\n"
                     "ztMwMdDWoasmZ8dUVJoTXVQy+XyP/ZgQacFdvMVSUbsFbg2ssz4EwcwNYzSeJqXl\n"
                     "V2YprAhAmx4M0QpAR/csSfgMfBNJyhxLxRSKzonP892bx2Ks\n"
                     "-----END CERTIFICATE-----\n";

const char *keystr = "-----BEGIN RSA PRIVATE KEY-----\n"
                     "MIIEowIBAAKCAQEAwuGuH/kLqrGV2gAdaXJXBNrXncMvKVjA3w2JF5LXsvwpuqNy\n"
                     "h2MsOdIkyV8UyD7DKDAcnbxtKvfjCdV5gWsuoxWlVlIkxukPbfxq8kQ3jeCa/C5R\n"
                     "vLqfKTFahGVOcA3my75UGDMQjDqLaHN+1RCgO5AmbrVLW8BAcepe/L9GoVL6UXj/\n"
                     "U2sQICSCVfjUXBu7X0yi3lKuJX+YfKGVdfvrGvI4jfxrsYjwhyRzbudeeJrtkQya\n"
                     "huM2RkqDDN8Yg0TFzP4Xa9O7bJCkF6gQnMIMjO3zWU32uX+oT6fPHH9B3LUKq0I9\n"
                     "mRHglTtBmkTLPyCZ0WQ20l/AU9ytb2FEUtKHBQIDAQABAoIBAQCCKTGfcTt1mn63\n"
                     "x/PdBd1RBMmWOVM6jmjjkarK7+zZQsqsOZa5DmfvhofDZ5n91M2L/fZe9AcF9+LQ\n"
                     "IqCw6+GOU1rGuL6PjAIeN8VGYoGoZSee32EAaLT9UlVesDsfZGmOE2UfdJvMzR1n\n"
                     "ehxwOlaK8dB5iOT4NwPUEfG3ZhnKjPB8HNQxifyXnzO3/CiBUBcPpxwwDix+My4F\n"
                     "SE/vxG6c13JBeGe3Nt++lVVUa2yTMbMR2MPoOWImEnbBiJR4iYPMYg1F+RnKkgch\n"
                     "xIRCpNKno96Xg3U4X9RcA5MzmvK/HVSUKg0Vig9z+5Ot5817BUZ3X/xHYf47GaB7\n"
                     "Utl+D2iBAoGBAP49itYzmVmv/5/T4l81cU1eKScfvEwda35QmF4LBfH2punGqLGm\n"
                     "RvhJdno4iDZzwDBKLnoTYgQe2WubP9WKWH+YDTrlh0UpEm1hZw3ZGk1TDij20Upd\n"
                     "upbWaC7s92z9ZBEUivXbJu36kpOkJObe/VnxNEx5DF2ZMkDiGNbqAQTdAoGBAMQ6\n"
                     "95YsTH66R9zUA5lnLspdgjTXoOgxlNCAc3zxtTRwIBS7nVExy7goCKwee2kP7gyt\n"
                     "tV0/DEJSjaCaQuP8g5WYQsEDX0ukTFnryAdgt0XhAlqQs5MYD8LG/K1EhpnuT7am\n"
                     "X9+GMsjhemKH2Tj5lfz3GVGjDNftLJEoaYBsZHRJAoGAAVicEpRFXJc0+Eir6ysi\n"
                     "RXGZMC7X1WNWfV254M2YI3bV3WkC3aXuvEPPT8ha2Eb2norWAil2HGV5aztwNBY+\n"
                     "b7aDY2txsukLwu6AGC+tFvm4mnjsFMO4SDIsbQsuKDKTRA/iyHh3lUz9V7DHzT0j\n"
                     "BEXlq/38FcbI4FUSIpksljUCgYAHihR1KqKNw+5655Jz1GkR0WHtUdOW2EDVefn1\n"
                     "9Pt3Vk+FPGgg9H7VRoR+yHUJZllJF2t+d6uq6K0UXJjxVYRgvQbjj5ObvmZIliyL\n"
                     "TAX+o1SJ0kF0B1qjqy2OIrHhPCzH4cCRQAC3gyJGot2PuNcwbvYEEvWtXil2Mk1L\n"
                     "Z968MQKBgAcYDRdk79GNNi5IMOtsB+LyEjCSWYal0G9jddq6wirsd6AGNCMU3UNV\n"
                     "7d7xSiQdBSXNQcklCCrLmadhX0RDU0f6giB/AIrrz4vc06huj9rLAgwHto/J9ovd\n"
                     "j1LOyqMCEVm1zgXGLqubKK4xrL4sBDOH+wkKuKHIuG5e+jD2ikeo\n"
                     "-----END RSA PRIVATE KEY-----\n";

char *SK_TR369_API_GetCACertString() {
    int size = 2048;
    char *ret = static_cast<char *>(malloc(size));
    if (ret == nullptr) return nullptr;
    strcpy(ret, cacert);
    return ret;
}

char *SK_TR369_API_GetDevCertString() {
    int size = 2048;
    char *ret = static_cast<char *>(malloc(size));
    if (ret == nullptr) return nullptr;
    strcpy(ret, crtstr);
    return ret;
}

char *SK_TR369_API_GetDevKeyString() {
    int size = 3072;
    char *ret = static_cast<char *>(malloc(size));
    if (ret == nullptr) return nullptr;
    strcpy(ret, keystr);
    return ret;
}

#ifdef __cplusplus
}
#endif