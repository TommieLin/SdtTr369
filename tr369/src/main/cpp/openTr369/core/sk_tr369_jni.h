//
// Created by Outis on 2023/9/11.
//

#ifndef __SK_TR369_H__
#define __SK_TR369_H__

#ifdef __cplusplus
extern "C" {
#endif

//typedef int (*SK_TR369_Getter) (const char *path, const char *method,
//                                const char **value, unsigned int *len);

//typedef int (*SK_TR369_Setter) (const char *path, const char *method,
//                                const char *value, unsigned int len);

//void SK_TR369_Register_Setter_Getter(SK_TR369_Setter setter, SK_TR369_Getter getter);

int SK_TR369_Start(const char *const);
int SK_TR369_SetInitFilePath(const char *const);
char *SK_TR369_GetDefaultFilePath();

int SK_TR369_GetDBParam(const char *, char *);
int SK_TR369_SetDBParam(const char *, const char *);
int SK_TR369_AddMultiObject(const char *, int);
int SK_TR369_DelMultiObject(const char *);
int SK_TR369_ShowData(const char *);

char *SK_TR369_API_GetCACertString();
char *SK_TR369_API_GetDevCertString();
char *SK_TR369_API_GetDevKeyString();

#ifdef __cplusplus
}
#endif

#endif //__SK_TR369_H__
