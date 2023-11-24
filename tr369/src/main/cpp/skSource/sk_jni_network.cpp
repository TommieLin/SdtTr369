//
// Created by Outis on 2023/11/2.
//

#include <cstring>
#include <cstdlib>

#ifdef __cplusplus
extern "C" {
#endif

const char *cacert = "-----BEGIN CERTIFICATE-----\n"
                     "MIIDpzCCAo+gAwIBAgIJAKtucqL2zaL2MA0GCSqGSIb3DQEBCwUAMGoxCzAJBgNV\n"
                     "BAYTAkNOMRIwEAYDVQQIDAlHdWFuZ0RvbmcxCzAJBgNVBAcMAlNaMREwDwYDVQQK\n"
                     "DAhza3l3b3J0aDEPMA0GA1UECwwGc2t5b3ZzMRYwFAYDVQQDDA0xNzIuMjguMTEu\n"
                     "MjMxMB4XDTIzMDkyODA2MjYzMloXDTMzMDkyNTA2MjYzMlowajELMAkGA1UEBhMC\n"
                     "Q04xEjAQBgNVBAgMCUd1YW5nRG9uZzELMAkGA1UEBwwCU1oxETAPBgNVBAoMCHNr\n"
                     "eXdvcnRoMQ8wDQYDVQQLDAZza3lvdnMxFjAUBgNVBAMMDTE3Mi4yOC4xMS4yMzEw\n"
                     "ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDcdzG0gTKDx+VyagRucCRB\n"
                     "FUZ5YoiEXNghWhDnXXpOrRG1blpakyzVKGOL+bQjXvLN7RxWOuc9XVti7cWmzRcx\n"
                     "k56SR3NfNzzdQGYbOeOUewDHrGm+YuBBTimHAc9id6V8XokUu/X+DfAHLyPkOvmX\n"
                     "6ffZy7SaxADCOJ2FiLTVQX7a6IXPw7XaqtVkIxIIbxfo0cMJvsjz8cWGGV5Wswh6\n"
                     "FAaTlknvE4CEF6iyacY15lZ3M/+28tYZ6kvhFKQfdPysOIqMriUYsxHtefegM/Qo\n"
                     "jcg4ZI43LDRhsqX9s/tz09RpZZRLuP2pALFgHKJdOyzx+0yT+MzZ7CPa6WQFaZwx\n"
                     "AgMBAAGjUDBOMB0GA1UdDgQWBBQ0H1IEQZEQoyt3GvniGIAphJoOKzAfBgNVHSME\n"
                     "GDAWgBQ0H1IEQZEQoyt3GvniGIAphJoOKzAMBgNVHRMEBTADAQH/MA0GCSqGSIb3\n"
                     "DQEBCwUAA4IBAQBG2NvsLkCQpvx9Gaz2CPQQqOHHFuiFsTSOKiAXugU/4WywSgTR\n"
                     "WyU1ib7V+WUsG9ceOLMmkZqf+3J6IifEOdmcQK95q+D2UFE6Iw4CbTLqToFiFxsC\n"
                     "Tm5b3clDa5Pjn2BTow1Rnw4aMWfLubGi9T8LsJJV2R4EmtZ6LL6hr4kB/bAPO32g\n"
                     "tokOmtDB3xqW0HN41a+TgSg2bCHsEdnEc4leYCBkqrIt8dJJmw73mkbJbKXSUwvb\n"
                     "VnCSshRjl+SKUG41yqe8kCOqjAn3jCzTo0WcQAq/igfzZZg1xQ9xQvVFzU+jnMlZ\n"
                     "WtIE9ReMvt7rWhGQpZ13Fy5QuVixmveIVk73\n"
                     "-----END CERTIFICATE-----";

const char *crtstr = "-----BEGIN CERTIFICATE-----\n"
                     "MIIDcjCCAloCAQEwDQYJKoZIhvcNAQELBQAwajELMAkGA1UEBhMCQ04xEjAQBgNV\n"
                     "BAgMCUd1YW5nRG9uZzELMAkGA1UEBwwCU1oxETAPBgNVBAoMCHNreXdvcnRoMQ8w\n"
                     "DQYDVQQLDAZza3lvdnMxFjAUBgNVBAMMDTE3Mi4yOC4xMS4yMzEwHhcNMjMwOTI4\n"
                     "MDYzMzA4WhcNMzMwOTI1MDYzMzA4WjCBkzELMAkGA1UEBhMCQ04xEjAQBgNVBAgM\n"
                     "CUd1YW5nRG9uZzELMAkGA1UEBwwCU1oxETAPBgNVBAoMCHNreXdvcnRoMQ8wDQYD\n"
                     "VQQLDAZza3lvdnMxFjAUBgNVBAMMDTE3Mi4yOC4xMS4yMzExJzAlBgkqhkiG9w0B\n"
                     "CQEWGHpoYW5neGluZzAyQHNreXdvcnRoLmNvbTCCASIwDQYJKoZIhvcNAQEBBQAD\n"
                     "ggEPADCCAQoCggEBAK8HiUcyKWT/nyH3V0tlfPTsJ0JN0A2w4kPdiXluKLsunyaN\n"
                     "JOff4FTPYW7UmXSuFIqOdb+vV+BvaI6NWSFWrNT1BOzrj+5plLbHDcc7qpWRmjVX\n"
                     "qplp16RWacTBR4majuoC90P4qSsMyUeFNr2alUO/5MFnW/aOdKmobTuifk5UHJJl\n"
                     "Z7TRDV7gP5dbJOR6pe2HU4TPiFXEPXEv1BtxbKWFlfjARCAl8jE/BGYwxgPEntZF\n"
                     "VZC+VXnYSDXeQ2rCWSilLi39KlLQSdjUbl3EMfPMp3Pgf0K+fcyvhg7CWyM4YMoH\n"
                     "fjB0NylIvLnHReoDpvSei2VtGAA5tB0j4z1qjvkCAwEAATANBgkqhkiG9w0BAQsF\n"
                     "AAOCAQEALX0Ox3/aeKozUNR0en5XjTkFGJAvcJp8/UWYuRjCxEwANhKzgFZmgpcK\n"
                     "Ev7vs+n0BHakVbEMdkMUef/Xyy9913hQxEyEHcK1CCK9a0GnMimVUyWslCOx/vuf\n"
                     "0O9ydryjbocd2NhY0AHOqzbJIOBluiuuyRrmMaRhS3M4fhhJBnwO8GybJqcGjzO0\n"
                     "dybWhYf42CpEdeSP8POI854O89rvWj+ZcVfzYlYZJyqrSmS+5aWqTv+GT57+AbqW\n"
                     "YRGyS0kBIsNEEKe0JlnqYLSz6n/NvxiUXoMHv1w7naWlnv5a5MYR1gvphwY1OGpz\n"
                     "l2Ga5Ga8rCHOyfdZffbRrONuQlYqnQ==\n"
                     "-----END CERTIFICATE-----";

const char *keystr = "-----BEGIN RSA PRIVATE KEY-----\n"
                     "MIIEpAIBAAKCAQEArweJRzIpZP+fIfdXS2V89OwnQk3QDbDiQ92JeW4ouy6fJo0k\n"
                     "59/gVM9hbtSZdK4Uio51v69X4G9ojo1ZIVas1PUE7OuP7mmUtscNxzuqlZGaNVeq\n"
                     "mWnXpFZpxMFHiZqO6gL3Q/ipKwzJR4U2vZqVQ7/kwWdb9o50qahtO6J+TlQckmVn\n"
                     "tNENXuA/l1sk5Hql7YdThM+IVcQ9cS/UG3FspYWV+MBEICXyMT8EZjDGA8Se1kVV\n"
                     "kL5VedhINd5DasJZKKUuLf0qUtBJ2NRuXcQx88ync+B/Qr59zK+GDsJbIzhgygd+\n"
                     "MHQ3KUi8ucdF6gOm9J6LZW0YADm0HSPjPWqO+QIDAQABAoIBAFOsRKI2hrdzxD1W\n"
                     "ovK2R7BGnNYDoOyKnQBYjfnxAaPKO+cQHo6C5hllMmzrUZkIB2XdiMjkKBxw5gkP\n"
                     "5YYci28a2wnv6tTMwH1IV3vhOEFcY94QVMwWXzJ+5P1ccLiFnMCePlrOKwEbkbWR\n"
                     "J4QovCaxO8iBguMxYvAgRmf13G/Q15VHPOUVZjrinK+QzROGX33HpCodMFKRY/9D\n"
                     "04FSt0gqp8Wvm2XkH0yTTvkmU5wAzI84jSljNEE1z6FAm5VFXAmOs1+R7nmdkN6L\n"
                     "vzRFWeq0wdKwCQsvULnWsIeV/vwPjlc+d7M8RS72J6MFj/3lFxbfkUhc6xSds56D\n"
                     "eM/A7skCgYEA1zaw3mSo7g7vZVpT8vDM8siHF7l6ETfkaye6icFrpQ6wihRts5j5\n"
                     "FOqmun7R+XyPIv3axu31lq4Gkxwc/AvSC/fUmHNSiN9VyeUctc+pHmavnvt8li+E\n"
                     "hPtHDap1ANa/24Czt7FqzeuMQ7iqy6wUUP6W4dv9t4rnCa/KgRSAAwMCgYEA0DNE\n"
                     "m4QCDPlaaPgqmnUUjI83ObRYgFodJiIbkM9GmqQaQYpY/pi7DDzI3H1AAQyXH6SJ\n"
                     "gZAJZdemfeu3mO+ClfzO2FI+60nnWduGtnrAigw7zRIwAorlrgbeOpmOlgM0Vn4G\n"
                     "LNEj2WuISYl+PpCVsOOfj7eAggdmLSV3p5Zxh1MCgYEAy91Z5AFUaNWq/sZ3GX3z\n"
                     "ltRP7DBdqmvYq4zhzlZz4tIsd1BsrqFs6dxx/d3eh8fvS0VPdfu6FXdacQZDipqj\n"
                     "6YRlVdiASXnOKUcoUC2bVVoOaLpvxVDT5qilIRPka9wBuCuNkqe0tcW4g/otiGvE\n"
                     "fwj74o3jJHem7e0hFe9WaAMCgYEAhOzWlru3W3UwJeiburWZgwxE5BCimH/wMFKC\n"
                     "avgLSdwP71xW6WW1tmBwlWVaIdFaAxfXuuKtPVprr+V4+KmiJASeQzpvdWVf0dz4\n"
                     "qzTADOM2ov1DrbvkXDGCXMOBZ4FwPvCDHMYCL2QaRWkFMwi4qZEKZIVyBJKYx6Yu\n"
                     "OM8yeIUCgYAzEdc1+GQMgzKFbsjzXMUpXOXTCSSmWKYaAKFvbmsSw2JwfeOBoo0C\n"
                     "yMT0gJeHaOWKPTsEfmeESU5GnGiLbxRfxWT8ZvkfOv6vPS6xX7gIQxeLC0aMVdIU\n"
                     "iLKr5vuwRZEKdP/sCSSCfVfMJEw4sg4NOYDskKK+eSPVayNucega+Q==\n"
                     "-----END RSA PRIVATE KEY-----";

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