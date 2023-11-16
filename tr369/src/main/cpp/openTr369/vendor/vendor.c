/*
 *
 * Copyright (C) 2019, Broadband Forum
 * Copyright (C) 2016-2019  CommScope, Inc
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

/**
 * \file vendor.c
 *
 * Implements the interface to all vendor implemented data model nodes
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#include <libxml/parser.h>
#include <libxml/tree.h>

#include "usp_err_codes.h"
#include "vendor_defs.h"
#include "vendor_api.h"
#include "usp_api.h"
#include "usp_log.h"
#include "sk_tr369_jni.h"
#include "sk_jni_callback.h"
#include "data_model.h"
#include "database.h"
#include "dm_trans.h"


/*********************************************************************//**
**
** SK_TR369_Register_Setter_Getter
**
**
**
**
** \param
**
** \return  None
**
**************************************************************************/
//SK_TR369_Setter sk_tr369_jni_setter = NULL;
//SK_TR369_Getter sk_tr369_jni_getter = NULL;
//void SK_TR369_Register_Setter_Getter(SK_TR369_Setter setter, SK_TR369_Getter getter)
//{
//    sk_tr369_jni_setter = setter;
//    sk_tr369_jni_getter = getter;
//}

// Skyworth Customized Content
typedef struct
{
    char name[MAX_PATH_SEGMENTS];   // 节点的名字
    char path[MAX_DM_PATH];         // 节点完整路径
    unsigned type;                  // 节点存储的数据类型
//    int inform;
//    int app_inform;
//    char write;
    dm_get_value_cb_t getter;
    dm_set_value_cb_t setter;
    dm_notify_set_cb_t notification;
    char value[MAX_DM_VALUE_LEN];   // 默认的值 对应default
} sk_schema_node_t;

char *sk_tr369_model_xml = NULL;


int SK_TR369_GetVendorParam(dm_req_t *req, char *buf, int len)
{
    int err = USP_ERR_OK;
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_GetVendorParam start");
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_GetVendorParam req->path: %s, req->schema_path: %s", req->path, req->schema_path);
    err = SK_TR369_API_GetParams(req->path, buf, len);
    if (err == -1)
    {
        err = SK_TR369_GetDBParam(req->path, buf);
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_GetDBParam return: %d", err);
    }
    return err;
}


int SK_TR369_SetVendorParam(dm_req_t *req, char *buf)
{
    int err = USP_ERR_OK;
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetVendorParam start");
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetVendorParam req->path: %s, req->schema_path: %s", req->path, req->schema_path);
    err = SK_TR369_API_SetParams(req->path, buf);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_API_SetParams return: %d", err);
    if (err == -1)
    {
        err = SK_TR369_SetDBParam(req->path, buf);
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetDBParam return: %d", err);
    }
    return err;
}


void SK_TR369_GetNodeFullName(xmlNodePtr node, char *name)
{
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_GetNodeFullName start");
    xmlNodePtr current = node;
    if (node == NULL || name == NULL)
    {
        USP_LOG_Error("%s: Parameter error.", __FUNCTION__);
        return;
    }
    while (current != NULL)
    {
        xmlChar *node_name = xmlGetProp(current, (const xmlChar *)"name");
        USP_LOG_Info(" ######### Outis ~~~ GetNodeFullName nodeName: %s", node_name);
        if (node_name == NULL)
        {
            USP_LOG_Info(" ######### Outis ~~~ nodeName == NULL");
            break;
        }

        xmlChar fullName[MAX_DM_PATH] = {0};
        if (name[0] != '\0')
        {
            sprintf((char *)fullName, "%s.%s", node_name, name);
            USP_LOG_Info(" ######### Outis ~~~ GetNodeFullName fullName: %s", fullName);
        }
        else
        {
            sprintf((char *)fullName, "%s", node_name);
        }

        sprintf(name, "%s", fullName);
        USP_LOG_Info(" ######### Outis ~~~ GetNodeFullName Name: %s", name);

        xmlFree(node_name);
        current = current->parent;
    }
}


void SK_TR369_ParseNode(xmlNodePtr xml_node, sk_schema_node_t *schema_node)
{
    if (schema_node == NULL)
    {
        USP_LOG_Error("%s: Parameter error.", __FUNCTION__);
        return;
    }

    SK_TR369_GetNodeFullName(xml_node, schema_node->path);
    xmlChar *name = xmlGetProp(xml_node, (const xmlChar *)"name");
    xmlChar *getter = xmlGetProp(xml_node, (const xmlChar *)"getter");
    xmlChar *setter = xmlGetProp(xml_node, (const xmlChar *)"setter");
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_ParseNode Path: %s, Name: %s, Getter: %s, Setter: %s", schema_node->path, name, getter, setter);

    if (name != NULL) sprintf(schema_node->name, "%s", name);
    schema_node->getter = (getter != NULL && (xmlStrcmp(getter, (const xmlChar *)"diagnose") == 0)) ? SK_TR369_GetVendorParam : NULL;
    schema_node->setter = (setter != NULL && (xmlStrcmp(setter, (const xmlChar *)"diagnose") == 0)) ? SK_TR369_SetVendorParam : NULL;

    xmlChar *default_value = xmlGetProp(xml_node, (const xmlChar *)"default");
    if (default_value != NULL) sprintf(schema_node->value, "%s", default_value);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_ParseNode Path: %s, Name: %s, Default_Value: %s", schema_node->path, name, schema_node->value);

//    xmlChar *inform = xmlGetProp(xml_node, (const xmlChar *)"inform");
//    if (inform != NULL)
//    {
//        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_ParseNode inform != NULL");
//        if (xmlStrcmp(inform, (const xmlChar *)"true") == 0)
//        {
//            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_ParseNode inform == true");
//            schema_node->inform = 1;
//        }
//    }
//    xmlFree(inform);
    xmlFree(name);
    xmlFree(getter);
    xmlFree(setter);
}


void SK_TR369_ParseType(xmlChar *type, sk_schema_node_t *schema_node)
{
    if (schema_node == NULL)
    {
        USP_LOG_Error("%s: Parameter error.", __FUNCTION__);
        return;
    }

    if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"string"))
    {
        schema_node->type = DM_STRING;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"boolean"))
    {
        schema_node->type = DM_BOOL;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"dateTime"))
    {
        schema_node->type = DM_DATETIME;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"int"))
    {
        schema_node->type = DM_INT;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"unsignedInt"))
    {
        schema_node->type = DM_UINT;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"long"))
    {
        schema_node->type = DM_LONG;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"unsignedLong"))
    {
        schema_node->type = DM_ULONG;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"base64"))
    {
        schema_node->type = DM_BASE64;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"hex"))
    {
        schema_node->type = DM_HEXBIN;
    }
    else if (type != NULL && !xmlStrcmp(type, (const xmlChar *)"decimal"))
    {
        schema_node->type = DM_DECIMAL;
    }
    else
    {
        schema_node->type = DM_STRING;
    }
}


int SK_TR369_AddNodeToUspDataModel(sk_schema_node_t *schema_node)
{
    int err = USP_ERR_OK;

    if (schema_node == NULL)
    {
        USP_LOG_Error("%s: Parameter error.", __FUNCTION__);
        return USP_ERR_INTERNAL_ERROR;
    }

    if (schema_node->setter != NULL)
    {
        if (schema_node->getter != NULL)
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter != NULL, setter != NULL", schema_node->path);
            err |= USP_REGISTER_VendorParam_ReadWrite(schema_node->path, schema_node->getter, schema_node->setter, NULL, DM_STRING);
        }
        else
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter == NULL, setter != NULL", schema_node->path);
            err |= USP_REGISTER_VendorParam_ReadWrite(schema_node->path, SK_TR369_GetVendorParam, schema_node->setter, NULL, DM_STRING);
        }
    }
    else
    {
        if (schema_node->getter != NULL)
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter != NULL, setter == NULL", schema_node->path);
            err |= USP_REGISTER_VendorParam_ReadOnly(schema_node->path, schema_node->getter, DM_STRING);
        }
        else
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter == NULL, setter == NULL", schema_node->path);
            if (strlen(schema_node->value) != 0)
            {
                USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, value: %s, type: 0x%08X", schema_node->path, schema_node->value, schema_node->type);
                err |= USP_REGISTER_DBParam_ReadOnly(schema_node->path, schema_node->value, schema_node->type);
            }
        }
    }

    return err;
}


int SK_TR369_AddNodeToBootParameter(sk_schema_node_t *schema_node, int num)
{
    int err = USP_ERR_OK;
    char enable_path[MAX_DM_PATH], param_path[MAX_DM_PATH];

    if (schema_node == NULL)
    {
        USP_LOG_Error("%s: Parameter error.", __FUNCTION__);
        return USP_ERR_INTERNAL_ERROR;
    }

    USP_SNPRINTF(enable_path, sizeof(enable_path), "Device.LocalAgent.Controller.1.BootParameter.%d.Enable", num);
    USP_SNPRINTF(param_path, sizeof(param_path), "Device.LocalAgent.Controller.1.BootParameter.%d.ParameterName", num);

    USP_LOG_Info(" ######### Outis *** enable_path: %s", enable_path);
    err |= DATA_MODEL_SetParameterInDatabase(enable_path, "true");
    USP_LOG_Info(" ######### Outis *** param_path: %s, schema_node->path: %s", param_path, schema_node->path);
    err |= DATA_MODEL_SetParameterInDatabase(param_path, schema_node->path);

    USP_LOG_Info(" ######### Outis *** SK_TR369_AddNodeToBootParameter return: %d", err);
    return err;
}

//static int boot_param_number = 0;

void SK_TR369_ParseSchema(xmlNodePtr node)
{
    xmlNodePtr current = node;
    while (current != NULL)
    {
        if (xmlStrcmp(current->name, (const xmlChar *)"schema") == 0)
        {
            xmlChar *type = xmlGetProp(current, (const xmlChar *)"type");

            if (!xmlStrcmp(type, (const xmlChar *)"multipleObject"))
            {
                USP_LOG_Info(" ************ Outis *** multipleObject find!!!");
                char node_path[MAX_DM_PATH] = {0};
                SK_TR369_GetNodeFullName(current, node_path);
                USP_LOG_Info(" ######### Outis *** multipleObject node_path: %s", node_path);
                USP_REGISTER_Object(node_path, NULL, NULL, NULL, NULL, NULL, NULL);
            }
            else if (!xmlStrcmp(type, (const xmlChar *)"multipleNumber"))
            {
                xmlChar *table = xmlGetProp(current, (const xmlChar *)"table");
                if (table != NULL)
                {
                    char node_path[MAX_DM_PATH] = {0};
                    char table_path[MAX_DM_PATH] = {0};
                    sprintf(table_path, "%s", table);
                    SK_TR369_GetNodeFullName(current, node_path);
                    USP_LOG_Info(" ######### Outis *** multipleNumber node_path: %s, table_path: %s", node_path, table_path);
                    USP_REGISTER_Param_NumEntries(node_path, table_path);
                    free(table);
                }
            }
            else if (xmlStrcmp(type, (const xmlChar *)"object")
                    && xmlStrcmp(type, (const xmlChar *)"unknown"))
            {
                sk_schema_node_t schema_node = {0};
                SK_TR369_ParseNode(current, &schema_node);
                SK_TR369_ParseType(type, &schema_node);
                SK_TR369_AddNodeToUspDataModel(&schema_node);

//                // 判断该节点是否需要放到启动参数里上报(该判断交由服务端决定，即由服务端下发指令设置BootParameter)
//                if (schema_node.inform == 1)
//                {
//                    boot_param_number++;
//                    SK_TR369_AddNodeToBootParameter(&schema_node, boot_param_number);
//                }
            }

            xmlFree(type);
        }
        SK_TR369_ParseSchema(current->children);
        current = current->next;
    }
}


int SK_TR369_ParseModelFile(void)
{
    if (sk_tr369_model_xml == NULL)
    {
        USP_LOG_Error("%s: Model file path not initialized.", __FUNCTION__);
        return USP_ERR_SK_INIT_FAILURE;
    }

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_ParseModelFile sk_tr369_model_xml: %s", sk_tr369_model_xml);

    xmlDocPtr doc = xmlReadFile(sk_tr369_model_xml, "UTF-8", XML_PARSE_RECOVER);
    if (doc == NULL)
    {
        USP_LOG_Error("%s: Failed to read tr369 model file (%s)", __FUNCTION__, sk_tr369_model_xml);
        return USP_ERR_INTERNAL_ERROR;
    }
    USP_LOG_Info(" ######### Outis ~~~~~~~~~ 1 ~~~~~~~~~~~ ");

    xmlNodePtr root = xmlDocGetRootElement(doc);
    USP_LOG_Info(" ######### Outis ~~~~~~~~~ 2 ~~~~~~~~~~~ ");

    SK_TR369_ParseSchema(root);
    USP_LOG_Info(" ######### Outis ~~~~~~~~~ 3 ~~~~~~~~~~~ ");

    xmlFreeDoc(doc);
    xmlCleanupParser();

    return USP_ERR_OK;
}

//------------------------------------------------------------------------------------
// Array of valid input arguments
static char *upload_file_input_args[] =
{
    "CommandKey",
    "FileType",
    "DelaySeconds",
    "Url",
};

static char *upgrade_file_input_args[] =
{
    "TargetFile",
    "FileSize",
    "Url",
};

static char *download_file_input_args[] =
{
    "CommandKey",
    "FileType",
    "Url",
};

//------------------------------------------------------------------------------------
// Array of valid output arguments
static char *x_event_output_args[] =
{
    "Status",
};

int SK_TR369_Start_UploadFile(dm_req_t *req, char *command_key, kv_vector_t *input_args, kv_vector_t *output_args)
{
    int err = USP_ERR_OK;
    char param[1024] = {0};
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_UploadFile start");
    // Input variables
    char *input_command_key, *input_file_type, *input_delay_seconds, *input_url;

    // Extract the input arguments using KV_VECTOR_ functions
    input_command_key = USP_ARG_Get(input_args, "CommandKey", "");
    input_file_type = USP_ARG_Get(input_args, "FileType", "");
    input_delay_seconds = USP_ARG_Get(input_args, "DelaySeconds", "");
    input_url = USP_ARG_Get(input_args, "Url", "");

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_UploadFile CommandKey: %s, FileType: %s, DelaySeconds: %s, Url: %s",
            input_command_key, input_file_type, input_delay_seconds, input_url);

    if (strcmp(input_command_key, "") == 0
            || strcmp(input_file_type, "") == 0
            || strcmp(input_delay_seconds, "") == 0
            || strcmp(input_url, "") == 0)
    {
        // if it doesn't, return invalid value
        USP_ERR_SetMessage("%s: Invalid value - The parameters for uploading files are empty.", __FUNCTION__);
        err = USP_ERR_INVALID_VALUE;
        goto exit;
    }

    strcpy(param, "UploadFile###");
    strcat(param, input_command_key);
    strcat(param, "###");
    strcat(param, input_file_type);
    strcat(param, "###");
    strcat(param, input_delay_seconds);
    strcat(param, "###");
    strcat(param, input_url);

    int res = SK_TR369_API_SendEvent(param);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_UploadFile SendEvent res: %d", res);

    // Save all results into the output arguments using KV_VECTOR_ functions
    USP_ARG_Add(output_args, "Status", "Complete");

exit:
    return err;
}


int SK_TR369_Start_UpgradeFile(dm_req_t *req, char *command_key, kv_vector_t *input_args, kv_vector_t *output_args)
{
    int err = USP_ERR_OK;
    char param[1024] = {0};
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_UpgradeFile start");
    // Input variables
    char *input_target_file, *input_file_size, *input_url;

    // Extract the input arguments using KV_VECTOR_ functions
    input_target_file = USP_ARG_Get(input_args, "TargetFile", "");
    input_file_size = USP_ARG_Get(input_args, "FileSize", "");
    input_url = USP_ARG_Get(input_args, "Url", "");

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_UpgradeFile TargetFile: %s, FileSize: %s, Url: %s",
                 input_target_file, input_file_size, input_url);

    if (strcmp(input_target_file, "") == 0
        || strcmp(input_file_size, "") == 0
        || strcmp(input_url, "") == 0)
    {
        // if it doesn't, return invalid value
        USP_ERR_SetMessage("%s: Invalid value - The parameters for upgrading files are empty.", __FUNCTION__);
        err = USP_ERR_INVALID_VALUE;
        goto exit;
    }

    strcpy(param, "UpgradeFile###");
    strcat(param, input_url);
    strcat(param, "###");
    strcat(param, input_target_file);
    strcat(param, "###");
    strcat(param, input_file_size);

    int res = SK_TR369_API_SendEvent(param);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_UpgradeFile SendEvent res: %d", res);

    // Save all results into the output arguments using KV_VECTOR_ functions
    USP_ARG_Add(output_args, "Status", "Complete");

exit:
    return err;
}

int SK_TR369_Start_DownloadFile(dm_req_t *req, char *command_key, kv_vector_t *input_args, kv_vector_t *output_args)
{
    int err = USP_ERR_OK;
    char param[1024] = {0};
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_DownloadFile start");
    // Input variables
    char *input_command_key, *input_file_type, *input_url;

    // Extract the input arguments using KV_VECTOR_ functions
    input_command_key = USP_ARG_Get(input_args, "CommandKey", "");
    input_file_type = USP_ARG_Get(input_args, "FileType", "");
    input_url = USP_ARG_Get(input_args, "Url", "");

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_UploadFile CommandKey: %s, FileType: %s, Url: %s",
                 input_command_key, input_file_type, input_url);

    if (strcmp(input_command_key, "") == 0
        || strcmp(input_file_type, "") == 0
        || strcmp(input_url, "") == 0)
    {
        // if it doesn't, return invalid value
        USP_ERR_SetMessage("%s: Invalid value - The parameters for downloading files are empty.", __FUNCTION__);
        err = USP_ERR_INVALID_VALUE;
        goto exit;
    }

//    if (strcmp(input_file_type, "3 Vendor Configuration File") != 0)
//    {
//        USP_ERR_SetMessage("%s: The file type (%s) does not match.", __FUNCTION__, input_file_type);
//        err = USP_ERR_INVALID_VALUE;
//        goto exit;
//    }

    strcpy(param, "DownloadFile###");
    strcat(param, input_command_key);
    strcat(param, "###");
    strcat(param, input_url);
    strcat(param, "###");
    strcat(param, input_file_type);

    int res = SK_TR369_API_SendEvent(param);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_DownloadFile SendEvent res: %d", res);

    // Save all results into the output arguments using KV_VECTOR_ functions
    USP_ARG_Add(output_args, "Status", "Complete");

exit:
    return err;
}

//------------------------------------------------------------------------------------
// Array of valid input arguments
static char *ip_ping_input_args[] =
{
    "Host",
    "DataBlockSize",
    "NumberOfRepetitions",
    "Timeout",      // 单位毫秒
    // Not used.
    "DSCP",
    "Interface",
    "ProtocolVersion",
};

//------------------------------------------------------------------------------------
// Array of valid output arguments
static char *ip_ping_output_args[] =
{
    "Status",
    "SuccessCount",
    "FailureCount",
    "AverageResponseTime",
    "MinimumResponseTime",
    "MaximumResponseTime",
    "AverageResponseTimeDetailed",
    "MinimumResponseTimeDetailed",
    "MaximumResponseTimeDetailed",
    "IPAddressUsed",
};

int SK_TR369_Start_IPPing(dm_req_t *req, char *command_key, kv_vector_t *input_args, kv_vector_t *output_args)
{
    int err = USP_ERR_OK;
    char param[1024] = {0};
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_IPPing start");
    // Input variables
    char *input_host, *input_size, *input_count, *input_timeout_ms;

    // Extract the input arguments using KV_VECTOR_ functions
    input_host = USP_ARG_Get(input_args, "Host", "");
    input_size = USP_ARG_Get(input_args, "DataBlockSize", "");
    input_count = USP_ARG_Get(input_args, "NumberOfRepetitions", "");
    input_timeout_ms = USP_ARG_Get(input_args, "Timeout", "");

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_IPPing Host: %s, DataBlockSize: %s, NumberOfRepetitions: %s, Timeout: %s",
                 input_host, input_size, input_count, input_timeout_ms);

    if (strcmp(input_host, "") == 0
        || strcmp(input_size, "") == 0
        || strcmp(input_count, "") == 0
        || strcmp(input_timeout_ms, "") == 0)
    {
        // if it doesn't, return invalid value
        USP_ERR_SetMessage("%s: Invalid value - The parameters for IPPing() are empty.", __FUNCTION__);
        err = USP_ERR_INVALID_VALUE;
        goto exit;
    }

    strcpy(param, "IPPing###");
    strcat(param, input_host);
    strcat(param, "###");
    strcat(param, input_size);
    strcat(param, "###");
    strcat(param, input_count);
    strcat(param, "###");
    strcat(param, input_timeout_ms);

    int res = SK_TR369_API_SendEvent(param);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_IPPing SendEvent res: %d", res);

    // Save all results into the output arguments using KV_VECTOR_ functions
    char status[16], ipAddressUsed[16], successCount[8], failureCount[8], avg[8], min[8], max[8], avg_ns[8], min_ns[8], max_ns[8];
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.Status", status);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.IPAddressUsed", ipAddressUsed);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.SuccessCount", successCount);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.FailureCount", failureCount);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.AverageResponseTime", avg);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.MinimumResponseTime", min);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.MaximumResponseTime", max);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.AverageResponseTimeDetailed", avg_ns);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.MinimumResponseTimeDetailed", min_ns);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.IPPing.MaximumResponseTimeDetailed", max_ns);

    USP_ARG_Add(output_args, "Status", status);
    USP_ARG_Add(output_args, "SuccessCount", successCount);
    USP_ARG_Add(output_args, "FailureCount", failureCount);
    USP_ARG_Add(output_args, "AverageResponseTime", avg);
    USP_ARG_Add(output_args, "MinimumResponseTime", min);
    USP_ARG_Add(output_args, "MaximumResponseTime", max);
    USP_ARG_Add(output_args, "AverageResponseTimeDetailed", avg_ns);
    USP_ARG_Add(output_args, "MinimumResponseTimeDetailed", min_ns);
    USP_ARG_Add(output_args, "MaximumResponseTimeDetailed", max_ns);
    USP_ARG_Add(output_args, "IPAddressUsed", ipAddressUsed);

exit:
    return err;
}

//------------------------------------------------------------------------------------
// Array of valid input arguments
static char *trace_route_input_args[] =
{
    "Host",
    "Timeout",
    "MaxHopCount",
    "DataBlockSize",
    // Not used.
    "DSCP",
    "Interface",
    "ProtocolVersion",
};

//------------------------------------------------------------------------------------
// Array of valid output arguments
static char *trace_route_output_args[] =
{
    "Status",
    "ResponseTime",
    "RouteHops.",
};

int SK_TR369_Start_TraceRoute(dm_req_t *req, char *command_key, kv_vector_t *input_args, kv_vector_t *output_args)
{
    int i, err = USP_ERR_OK;
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_TraceRoute start");
    // Input variables
    char *input_host, *input_timeout, *input_max_hop_count, *input_size;

    // Extract the input arguments using KV_VECTOR_ functions
    input_host = USP_ARG_Get(input_args, "Host", "");
    input_timeout = USP_ARG_Get(input_args, "Timeout", "");
    input_max_hop_count = USP_ARG_Get(input_args, "MaxHopCount", "");
    input_size = USP_ARG_Get(input_args, "DataBlockSize", "");

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_TraceRoute Host: %s, Timeout: %s, MaxHopCount: %s, DataBlockSize: %s",
                 input_host, input_timeout, input_max_hop_count, input_size);

    if (strcmp(input_host, "") == 0
        || strcmp(input_timeout, "") == 0
        || strcmp(input_max_hop_count, "") == 0
        || strcmp(input_size, "") == 0)
    {
        // if it doesn't, return invalid value
        USP_ERR_SetMessage("%s: Invalid value - The parameters for TraceRoute() are empty.", __FUNCTION__);
        err = USP_ERR_INVALID_VALUE;
        goto exit;
    }

    SK_TR369_SetDBParam("Device.IP.Diagnostics.TraceRoute.Host", input_host);
    SK_TR369_SetDBParam("Device.IP.Diagnostics.TraceRoute.Timeout", input_timeout);
    SK_TR369_SetDBParam("Device.IP.Diagnostics.TraceRoute.MaxHopCount", input_max_hop_count);
    SK_TR369_SetDBParam("Device.IP.Diagnostics.TraceRoute.DataBlockSize", input_size);

    int res = SK_TR369_API_SendEvent("TraceRoute");
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_TraceRoute SendEvent res: %d", res);

    // Save all results into the output arguments using KV_VECTOR_ functions
    char status[32], responseTime[8];
    SK_TR369_GetDBParam("Device.IP.Diagnostics.TraceRoute.Status", status);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.TraceRoute.ResponseTime", responseTime);

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_TraceRoute Status: %s, ResponseTime: %s", status, responseTime);

    USP_ARG_Add(output_args, "Status", status);
    USP_ARG_Add(output_args, "ResponseTime", responseTime);

    int_vector_t iv;
    INT_VECTOR_Init(&iv);
    err = DATA_MODEL_GetInstances("Device.IP.Diagnostics.TraceRoute.RouteHops", &iv);
    if (err != USP_ERR_OK)
    {
        INT_VECTOR_Destroy(&iv);
        goto exit;
    }

    for (i = 0; i < iv.num_entries; i++)
    {
        char output_host[32], output_host_path[32], host_path[MAX_DM_PATH] = {0};
        USP_SNPRINTF(output_host_path, sizeof(output_host_path), "RouteHops.%d.Host", iv.vector[i]);
        USP_SNPRINTF(host_path, sizeof(host_path), "Device.IP.Diagnostics.TraceRoute.%s", output_host_path);
        SK_TR369_API_GetParams(host_path, output_host, sizeof(output_host));

        char output_address[32], output_address_path[32], address_path[MAX_DM_PATH] = {0};
        USP_SNPRINTF(output_address_path, sizeof(output_address_path), "RouteHops.%d.HostAddress", iv.vector[i]);
        USP_SNPRINTF(address_path, sizeof(address_path), "Device.IP.Diagnostics.TraceRoute.%s", output_address_path);
        SK_TR369_API_GetParams(address_path, output_address, sizeof(output_address));

        char output_time[32], output_time_path[32], time_path[MAX_DM_PATH] = {0};
        USP_SNPRINTF(output_time_path, sizeof(output_time_path), "RouteHops.%d.RTTimes", iv.vector[i]);
        USP_SNPRINTF(time_path, sizeof(time_path), "Device.IP.Diagnostics.TraceRoute.%s", output_time_path);
        SK_TR369_API_GetParams(time_path, output_time, sizeof(output_time));

        USP_ARG_Add(output_args, output_host_path, output_host);
        USP_ARG_Add(output_args, output_address_path, output_address);
        USP_ARG_Add(output_args, output_time_path, output_time);

        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_TraceRoute [%d] %s: %s, %s: %s, %s: %s",
                     i, output_host_path, output_host, output_address_path, output_address, output_time_path, output_time);
    }
    INT_VECTOR_Destroy(&iv);

exit:
    return err;
}


//------------------------------------------------------------------------------------
// Array of valid input arguments
static char *download_diagnostics_input_args[] =
{
    "DownloadURL",
    "TimeBasedTestDuration",
        // Not used.
    "Interface",
    "DSCP",
    "EthernetPriority",
    "TimeBasedTestMeasurementInterval",
    "TimeBasedTestMeasurementOffset",
    "ProtocolVersion",
    "NumberOfConnections",
    "EnablePerConnectionResults",
};

//------------------------------------------------------------------------------------
// Array of valid output arguments
static char *download_diagnostics_output_args[] =
{
    "Status",
    "BOMTime",
    "EOMTime",
    "TestBytesReceived",
};

int SK_TR369_Start_DownloadDiagnostics(dm_req_t *req, char *command_key, kv_vector_t *input_args, kv_vector_t *output_args)
{
    int err = USP_ERR_OK;
    char param[1024] = {0};
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_DownloadDiagnostics start");
    // Input variables
    char *input_download_url, *input_duration;

    // Extract the input arguments using KV_VECTOR_ functions
    input_download_url = USP_ARG_Get(input_args, "DownloadURL", "");
    input_duration = USP_ARG_Get(input_args, "TimeBasedTestDuration", "");

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_DownloadDiagnostics DownloadURL: %s, TimeBasedTestDuration: %s",
                 input_download_url, input_duration);

    if (strcmp(input_download_url, "") == 0
        || strcmp(input_duration, "") == 0)
    {
        // if it doesn't, return invalid value
        USP_ERR_SetMessage("%s: Invalid value - The parameters for DownloadDiagnostics() are empty.", __FUNCTION__);
        err = USP_ERR_INVALID_VALUE;
        goto exit;
    }

    strcpy(param, "DownloadDiagnostics###");
    strcat(param, input_download_url);
    strcat(param, "###");
    strcat(param, input_duration);

    int res = SK_TR369_API_SendEvent(param);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_DownloadDiagnostics SendEvent res: %d", res);

    // Save all results into the output arguments using KV_VECTOR_ functions
    char status[32], BOMTime[32], EOMTime[32], testBytesReceived[8];
    SK_TR369_GetDBParam("Device.IP.Diagnostics.DownloadDiagnostics.Status", status);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.DownloadDiagnostics.BOMTime", BOMTime);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.DownloadDiagnostics.EOMTime", EOMTime);
    SK_TR369_GetDBParam("Device.IP.Diagnostics.DownloadDiagnostics.TestBytesReceived", testBytesReceived);

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_Start_DownloadDiagnostics status: %s, BOMTime: %s, EOMTime: %s, testBytesReceived: %s",
                 status, BOMTime, EOMTime, testBytesReceived);

    USP_ARG_Add(output_args, "Status", status);
    USP_ARG_Add(output_args, "BOMTime", BOMTime);
    USP_ARG_Add(output_args, "EOMTime", EOMTime);
    USP_ARG_Add(output_args, "TestBytesReceived", testBytesReceived);

exit:
    return err;
}

int SK_TR369_InitCustomEvent()
{
    int err = USP_ERR_OK;
    // X_Skyworth.Upload()
    err |= USP_REGISTER_SyncOperation("Device.X_Skyworth.UploadFile()", SK_TR369_Start_UploadFile);
    err |= USP_REGISTER_OperationArguments("Device.X_Skyworth.UploadFile()",
            upload_file_input_args, NUM_ELEM(upload_file_input_args),
            x_event_output_args, NUM_ELEM(x_event_output_args));

    // X_Skyworth.UpgradeFile()
    err |= USP_REGISTER_SyncOperation("Device.X_Skyworth.UpgradeFile()", SK_TR369_Start_UpgradeFile);
    err |= USP_REGISTER_OperationArguments("Device.X_Skyworth.UpgradeFile()",
            upgrade_file_input_args, NUM_ELEM(upgrade_file_input_args),
            x_event_output_args, NUM_ELEM(x_event_output_args));

    // X_Skyworth.DownloadFile()
    err |= USP_REGISTER_SyncOperation("Device.X_Skyworth.DownloadFile()", SK_TR369_Start_DownloadFile);
    err |= USP_REGISTER_OperationArguments("Device.X_Skyworth.DownloadFile()",
            download_file_input_args, NUM_ELEM(download_file_input_args),
            x_event_output_args, NUM_ELEM(x_event_output_args));

    // Device.IP.Diagnostics.IPPing()
    err |= USP_REGISTER_SyncOperation("Device.IP.Diagnostics.IPPing()", SK_TR369_Start_IPPing);
    err |= USP_REGISTER_OperationArguments("Device.IP.Diagnostics.IPPing()",
            ip_ping_input_args, NUM_ELEM(ip_ping_input_args),
            ip_ping_output_args, NUM_ELEM(ip_ping_output_args));

    // Device.IP.Diagnostics.TraceRoute()
    err |= USP_REGISTER_SyncOperation("Device.IP.Diagnostics.TraceRoute()", SK_TR369_Start_TraceRoute);
    err |= USP_REGISTER_OperationArguments("Device.IP.Diagnostics.TraceRoute()",
            trace_route_input_args, NUM_ELEM(trace_route_input_args),
            trace_route_output_args, NUM_ELEM(trace_route_output_args));

    // Device.IP.Diagnostics.DownloadDiagnostics()
    err |= USP_REGISTER_SyncOperation("Device.IP.Diagnostics.DownloadDiagnostics()", SK_TR369_Start_DownloadDiagnostics);
    err |= USP_REGISTER_OperationArguments("Device.IP.Diagnostics.DownloadDiagnostics()",
            download_diagnostics_input_args, NUM_ELEM(download_diagnostics_input_args),
            download_diagnostics_output_args, NUM_ELEM(download_diagnostics_output_args));


    return err;
}

/*********************************************************************//**
**
** VENDOR_Init
**
** Initialises this component, and registers all parameters and vendor hooks, which it implements
**
** \param   None
**
** \return  USP_ERR_OK if successful
**
**************************************************************************/
int VENDOR_Init(void)
{
    int err = USP_ERR_OK;

    SK_TR369_ParseModelFile();
    USP_LOG_Info(" ######### Outis ~~~ VENDOR_Init return");
    SK_TR369_InitCustomEvent();

    return USP_ERR_OK;
}

char *sk_multi_object_map[] =
{
    "Device.DeviceInfo.TemperatureStatus.TemperatureSensor",
    "Device.DeviceInfo.FirmwareImage",
    "Device.Ethernet.Link",
    "Device.IP.Interface",
    "Device.IP.Interface.1.IPv4Address",
    "Device.WiFi.Radio",
    "Device.WiFi.SSID",
    "Device.WiFi.EndPoint",
    "Device.WiFi.EndPoint.1.Profile",
    "Device.Services.STBService",
    "Device.Services.STBService.1.AVPlayer",
    "Device.Services.STBService.1.Components.HDMI",
    "Device.Services.STBService.1.Components.AudioOutput",
    "Device.Services.STBService.1.Components.AudioDecoder",
    "Device.Services.STBService.1.Components.VideoOutput",
    "Device.Services.STBService.1.Components.VideoDecoder",
    "Device.Services.STBService.1.Capabilities.VideoDecoder.MPEG2Part2.ProfileLevel",
    "Device.Services.STBService.1.Capabilities.VideoDecoder.MPEG4Part2.ProfileLevel",
    "Device.Services.STBService.1.Capabilities.VideoDecoder.MPEG4Part10.ProfileLevel",
    "Device.USB.USBHosts.Host",
    "Device.USB.USBHosts.Host.1.Device"
//    "Device.IP.Diagnostics.TraceRoute.RouteHops",       // 由RouteHops()事件触发更新
//    "Device.DeviceInfo.ProcessStatus.Process",          // 该节点需要动态添加
//    "Device.WiFi.NeighboringWiFiDiagnostic.Result",     // 该节点需要动态添加
//    "Device.X_Skyworth.App",                    // 该节点需要动态添加
//    "Device.X_Skyworth.App.1.Permissions",      // 该节点需要动态添加
//    "Device.X_Skyworth.BluetoothDevice"         // 该节点需要动态添加
};

int SK_TR369_SetDefaultMultiObject()
{
    int i;
    int err;
    int instance = INVALID;
    int_vector_t iv;

    int map_size = sizeof(sk_multi_object_map) / sizeof(sk_multi_object_map[0]);
    for (i = 0; i < map_size; i++)
    {
        INT_VECTOR_Init(&iv);
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetDefaultMultiObject path: %s", sk_multi_object_map[i]);
        err = DATA_MODEL_GetInstances(sk_multi_object_map[i], &iv);
        if (err != USP_ERR_OK)
        {
            INT_VECTOR_Destroy(&iv);
            continue;
        }
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetDefaultMultiObject num_entries: %d", iv.num_entries);
        if (iv.num_entries == 0)
        {
            err = DATA_MODEL_AddInstance(sk_multi_object_map[i], &instance, 0);
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetDefaultMultiObject instance: %d", instance);
            if (err != USP_ERR_OK)
            {
                INT_VECTOR_Destroy(&iv);
                continue;
            }
        }
        INT_VECTOR_Destroy(&iv);
    }

    // 初始化 ProcessStatus.Process.{i} MultiObject节点
    char num_buf[MAX_DM_INSTANCE_ORDER] = {0};
    err = SK_TR369_DelMultiObject("Device.DeviceInfo.ProcessStatus.Process");
    if (err != USP_ERR_OK)
    {
        return err;
    }
    SK_TR369_API_GetParams("Device.DeviceInfo.ProcessStatus.ProcessNumberOfEntries", num_buf, sizeof(num_buf));
    int num = atoi(num_buf);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetDefaultMultiObject ProcessNumberOfEntries: %s(%d)", num_buf, num);
    if (num > 0)
    {
        err = SK_TR369_AddMultiObject("Device.DeviceInfo.ProcessStatus.Process", num);
        if (err != USP_ERR_OK)
        {
            return err;
        }
    }

    // 初始化 NeighboringWiFiDiagnostic.Result.{i} MultiObject节点
    err = SK_TR369_DelMultiObject("Device.WiFi.NeighboringWiFiDiagnostic.Result");
    if (err != USP_ERR_OK)
    {
        return err;
    }
    SK_TR369_API_GetParams("Device.WiFi.NeighboringWiFiDiagnostic.ResultNumberOfEntries", num_buf, sizeof(num_buf));
    num = atoi(num_buf);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetDefaultMultiObject ResultNumberOfEntries: %s(%d)", num_buf, num);
    if (num > 0)
    {
        err = SK_TR369_AddMultiObject("Device.WiFi.NeighboringWiFiDiagnostic.Result", num);
        if (err != USP_ERR_OK)
        {
            return err;
        }
    }

    return err;
}


/*********************************************************************//**
**
** VENDOR_Start
**
** Called after data model has been registered and after instance numbers have been read from the USP database
** Typically this function is used to seed the data model with instance numbers or
** initialise internal data structures which require the data model to be running to access parameters
**
** \param   None
**
** \return  USP_ERR_OK if successful
**
**************************************************************************/
int VENDOR_Start(void)
{
    int err = USP_ERR_OK;
    err = SK_TR369_SetDefaultMultiObject();

    return err;
}

/*********************************************************************//**
**
** VENDOR_Stop
**
** Called when stopping USP agent gracefully, to free up memory and shutdown
** any vendor processes etc
**
** \param   None
**
** \return  USP_ERR_OK if successful
**
**************************************************************************/
int VENDOR_Stop(void)
{

    return USP_ERR_OK;
}

int SK_TR369_GetDBParam(const char *param, char *value)
{
    int err;
    dm_hash_t hash;
    char instances[MAX_DM_PATH];
    unsigned path_flags;

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_GetDBParam param: %s", param);

    // Exit if parameter path is incorrect
    err = DM_PRIV_FormDB_FromPath(param, &hash, instances, sizeof(instances));
    if (err != USP_ERR_OK)
    {
        return err;
    }

    // Exit, not printing any value, if this parameter is obfuscated (eg containing a password)
    value[0] = '\0';
    path_flags = DATA_MODEL_GetPathProperties(param, INTERNAL_ROLE, NULL, NULL, NULL);
    if (path_flags & PP_IS_SECURE_PARAM)
    {
        goto exit;
    }

    // Exit if unable to get value of parameter from DB
    USP_ERR_ClearMessage();
    err = DATABASE_GetParameterValue(param, hash, instances, value, MAX_DM_VALUE_LEN, 0);
    if (err != USP_ERR_OK)
    {
        USP_LOG_Error("Parameter %s exists in the schema, but does not exist in the database", param);
        return err;
    }

exit:
    // Since successful, send back the value of the parameter
    USP_LOG_Info(" ######### Outis ~~~ %s => %s\n", param, value);

    return USP_ERR_OK;
}

int SK_TR369_SetDBParam(const char *param, const char *value)
{
    int err;

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetDBParam param: %s, value: %s", param, value);

    // Exit if unable to directly set the parameter in the database
    err = DATA_MODEL_SetParameterInDatabase(param, value);
    if (err != USP_ERR_OK)
    {
        return err;
    }

    // Since successful, send back the value of the parameter
    USP_LOG_Info(" ######### Outis ~~~ %s => %s\n", param, value);

    return USP_ERR_OK;
}

int SK_TR369_AddInstance(const char *param, int num)
{
    int i, err;
    int instance = INVALID;
    char path[MAX_DM_PATH];
    kv_vector_t unique_key_params;

    KV_VECTOR_Init(&unique_key_params);

    for (i = 0; i < num; i++)
    {
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddInstance for: %d", i);
        err = DATA_MODEL_AddInstance(param, &instance, 0);
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddInstance instance: %d", instance);
        if (err != USP_ERR_OK)
        {
            goto exit;
        }
        USP_SNPRINTF(path, sizeof(path), "%s.%d", param, instance);
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddInstance path: %s", path);

        // Exit if unable to retrieve the parameters used as unique keys for this object
        err = DATA_MODEL_GetUniqueKeyParams(path, &unique_key_params, INTERNAL_ROLE);
        if (err != USP_ERR_OK)
        {
            goto exit;
        }

        // Exit if any unique keys have been left with a default value which is not unique
        err = DATA_MODEL_ValidateDefaultedUniqueKeys(path, &unique_key_params, NULL);
        if (err != USP_ERR_OK)
        {
            goto exit;
        }
    }

exit:
    KV_VECTOR_Destroy(&unique_key_params);
    return err;
}

int SK_TR369_AddMultiObject(const char *param, int num)
{
    int err;
    dm_trans_vector_t trans;

    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddMultiObject num: %d", num);

    // Exit if unable to start a transaction
    err = DM_TRANS_Start(&trans);
    if (err != USP_ERR_OK)
    {
        DM_TRANS_Abort();
        return err;
    }

    err = SK_TR369_AddInstance(param, num);
    if (err != USP_ERR_OK)
    {
        DM_TRANS_Abort();
        return err;
    }

    err = DM_TRANS_Commit();
    if (err != USP_ERR_OK)
    {
        return err;
    }
    return USP_ERR_OK;
}

int SK_TR369_DeleteInstance(const char *param)
{
    int i, err;
    int_vector_t iv;

    INT_VECTOR_Init(&iv);
    err = DATA_MODEL_GetInstances(param, &iv);
    if (err != USP_ERR_OK)
    {
        goto exit;
    }

    for (i = 0; i < iv.num_entries; i++)
    {
        char path[MAX_DM_PATH] = {0};
        USP_SNPRINTF(path, sizeof(path), "%s.%d", param, iv.vector[i]);
        USP_LOG_Info(" ######### Outis ~~~ SK_TR369_DeleteInstance path: %s", path);
        err = DATA_MODEL_DeleteInstance(path, 0);
        if (err != USP_ERR_OK)
        {
            break;
        }
    }

exit:
    INT_VECTOR_Destroy(&iv);
    return err;
}

int SK_TR369_DelMultiObject(const char *param)
{
    int err;
    dm_trans_vector_t trans;
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_DelMultiObject param: %s", param);

    // Exit if unable to start a transaction
    err = DM_TRANS_Start(&trans);
    if (err != USP_ERR_OK)
    {
        DM_TRANS_Abort();
        return err;
    }

    err = SK_TR369_DeleteInstance(param);
    if (err != USP_ERR_OK)
    {
        DM_TRANS_Abort();
        return err;
    }

    // Exit if unable to commit the transaction
    err = DM_TRANS_Commit();
    if (err != USP_ERR_OK)
    {
        return err;
    }
    return USP_ERR_OK;
}

int SK_TR369_ShowData(const char *cmd)
{
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_ShowData cmd: %s", cmd);
    // Show the data model schema if required
    if (strcmp(cmd, "datamodel") == 0)
    {
        USP_DUMP("WARNING: This is the data model of this CLI command, rather than the daemon instance of this executable");
        USP_DUMP("If the data model does not contain 'Device.Test', then you are not running this CLI command with the '-T' option");
        DATA_MODEL_DumpSchema();
        return USP_ERR_OK;
    }

    // Show the contents of the database if required
    if (strcmp(cmd, "database") == 0)
    {
        DATABASE_Dump();
        return USP_ERR_OK;
    }

    return USP_ERR_INVALID_ARGUMENTS;
}
