/*
 *                       ######
 *                       ######
 * ############    ####( ######  #####. ######  ############   ############
 * #############  #####( ######  #####. ######  #############  #############
 *        ######  #####( ######  #####. ######  #####  ######  #####  ######
 * ###### ######  #####( ######  #####. ######  #####  #####   #####  ######
 * ###### ######  #####( ######  #####. ######  #####          #####  ######
 * #############  #############  #############  #############  #####  ######
 *  ############   ############  #############   ############  #####  ######
 *                                      ######
 *                               #############
 *                               ############
 *
 * Adyen Mirakl Connector
 *
 * Copyright (c) 2018 Adyen B.V.
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more info.
 *
 */

package com.adyen.mirakl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;

public final class MiraklShopFactory {
    private MiraklShopFactory() {
    }

    public static final Map<String, String> UBO_FIELDS_ENUMS = ImmutableMap.of("civility", "mr", "phonetype", "mobile", "dob", "1986-08-30T22:00:00Z");

    public static final Set<String> UBO_FIELDS = ImmutableSet.of("firstname",
                                                                 "lastname",
                                                                 "email",
                                                                 "nationality",
                                                                 "idnumber",
                                                                 "housenumber",
                                                                 "streetname",
                                                                 "city",
                                                                 "zip",
                                                                 "country",
                                                                 "phonecountry",
                                                                 "phonenumber");

    public static List<MiraklAdditionalFieldValue> createMiraklAdditionalUboField(String uboNumber, Set<String> uboFields, Map<String, String> uboEnumFields) {
        List<MiraklAdditionalFieldValue> additionalFieldValues = new ArrayList<>();
        uboFields.forEach(uboFieldName -> {
            MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalField = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
            additionalField.setCode("adyen-ubo" + uboNumber + "-" + uboFieldName);
            additionalField.setValue(uboFieldName + uboNumber);
            additionalFieldValues.add(additionalField);
        });
        uboEnumFields.forEach((k, v) -> {
            MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalField = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
            additionalField.setCode("adyen-ubo" + uboNumber + "-" + k);
            additionalField.setValue(v);
            additionalFieldValues.add(additionalField);
        });
        return additionalFieldValues;
    }
}
