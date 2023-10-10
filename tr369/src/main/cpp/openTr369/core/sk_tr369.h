//
// Created by SDT15755 on 2023/9/11.
//

#ifndef __SK_TR369_H__
#define __SK_TR369_H__

#ifdef __cplusplus
extern "C" {
#endif

int sk_tr369_start(const char *const);
int sk_set_db_file_path(const char *const);
char *sk_get_db_file_path();

#ifdef __cplusplus
}
#endif

#endif //__SK_TR369_H__
