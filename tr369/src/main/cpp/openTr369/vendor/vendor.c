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
    char name[MAX_PATH_SEGMENTS]; // name="InternetGatewayDevice" 属性的名字
    char path[MAX_DM_PATH];
    unsigned type; // object or multipleObject or something else
    int inform;
    int app_inform;
    char write;
    dm_get_value_cb_t getter;
    dm_set_value_cb_t setter;
    dm_notify_set_cb_t notification;
    char value[MAX_DM_VALUE_LEN];  //默认的值 对应default
} sk_schema_node_t;

char *sk_tr369_model_xml = NULL;


int SK_TR369_GetVendorParam(dm_req_t *req, char *buf, int len)
{
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_GetVendorParam start");
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_GetVendorParam req->path: %s, req->schema_path: %s", req->path, req->schema_path);
    SK_TR369_API_GetParams(req->path, buf, len);
    return USP_ERR_OK;
}


int SK_TR369_SetVendorParam(dm_req_t *req, char *buf)
{
    int err = USP_ERR_OK;
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetVendorParam start");
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_SetVendorParam req->path: %s, req->schema_path: %s", req->path, req->schema_path);
    err = SK_TR369_API_SetParams(req->path, buf);
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_API_SetParams return: %d", err);
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
        xmlChar *nodeName = xmlGetProp(current, (const xmlChar *)"name");
        USP_LOG_Info(" ######### Outis ~~~ GetNodeFullName nodeName: %s", nodeName);
        if (nodeName == NULL)
        {
            USP_LOG_Info(" ######### Outis ~~~ nodeName == NULL");
            break;
        }

        xmlChar fullName[MAX_DM_PATH] = {0};
        if (name[0] != '\0')
        {
            sprintf((char *)fullName, "%s.%s", nodeName, name);
            USP_LOG_Info(" ######### Outis ~~~ GetNodeFullName fullName: %s", fullName);
        }
        else
        {
            sprintf((char *)fullName, "%s", nodeName);
        }

        sprintf(name, "%s", fullName);
        USP_LOG_Info(" ######### Outis ~~~ GetNodeFullName Name: %s", name);

        xmlFree(nodeName);
        current = current->parent;
    }
}


int SK_TR369_AddNodeToUspDataModel(sk_schema_node_t *schema_node)
{
    int err = USP_ERR_OK;

    if (schema_node->setter != NULL)
    {
        if (schema_node->getter != NULL)
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter != NULL, setter != NULL", schema_node->path);
        }
        else
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter == NULL, setter != NULL", schema_node->path);
        }
        err |= USP_REGISTER_VendorParam_ReadWrite(schema_node->path, schema_node->getter, schema_node->setter, NULL, DM_STRING);
    }
    else
    {
        if (schema_node->getter != NULL)
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter != NULL, setter == NULL", schema_node->path);
        }
        else
        {
            USP_LOG_Info(" ######### Outis ~~~ SK_TR369_AddNodeToUspDataModel Path: %s, getter == NULL, setter == NULL", schema_node->path);
        }
        err |= USP_REGISTER_VendorParam_ReadOnly(schema_node->path, schema_node->getter, DM_STRING);
    }

    return err;
}


void SK_TR369_ParseNode(xmlNodePtr xml_node, sk_schema_node_t *schema_node)
{
    SK_TR369_GetNodeFullName(xml_node, schema_node->path);
    xmlChar *name = xmlGetProp(xml_node, (const xmlChar *)"name");
    xmlChar *getter = xmlGetProp(xml_node, (const xmlChar *)"getter");
    xmlChar *setter = xmlGetProp(xml_node, (const xmlChar *)"setter");
    USP_LOG_Info(" ######### Outis ~~~ SK_TR369_ParseNode Path: %s, Name: %s, Getter: %s, Setter: %s", schema_node->path, name, getter, setter);

    if (name != NULL) sprintf(schema_node->name, "%s", name);
    schema_node->getter = SK_TR369_GetVendorParam;
    schema_node->setter = (setter != NULL && (xmlStrcmp(setter, (const xmlChar *)"diagnose") == 0)) ? SK_TR369_SetVendorParam : NULL;

    xmlFree(name);
    xmlFree(getter);
    xmlFree(setter);
}


void SK_TR369_ParseSchema(xmlNodePtr node)
{
    xmlNodePtr current = node;
    while (current != NULL)
    {
        if (xmlStrcmp(current->name, (const xmlChar *)"schema") == 0)
        {
            xmlChar *type = xmlGetProp(current, (const xmlChar *)"type");

            if (xmlStrcmp(type, (const xmlChar *)"object") &&
                xmlStrcmp(type, (const xmlChar *)"multipleObject") &&
                xmlStrcmp(type, (const xmlChar *)"unknown"))
            {
                sk_schema_node_t schema_node = {0};
                SK_TR369_ParseNode(current, &schema_node);
                SK_TR369_AddNodeToUspDataModel(&schema_node);
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

    return USP_ERR_OK;
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

    return USP_ERR_OK;
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
