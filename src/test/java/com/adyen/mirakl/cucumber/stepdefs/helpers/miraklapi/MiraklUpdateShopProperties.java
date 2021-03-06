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

package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import com.adyen.mirakl.service.UboService;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.mirakl.client.domain.common.error.ErrorBean;
import com.mirakl.client.domain.common.error.InputWithErrors;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.common.document.MiraklDocumentsUploadResult;
import com.mirakl.client.mmp.domain.shop.MiraklProfessionalInformation;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShopAddress;
import com.mirakl.client.mmp.domain.shop.MiraklShopState;
import com.mirakl.client.mmp.domain.shop.bank.MiraklAbaBankAccountInformation;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.domain.shop.bank.MiraklPaymentInformation;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdateShop;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShops;
import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue;
import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue;
import com.mirakl.client.mmp.request.common.document.MiraklUploadDocument;
import com.mirakl.client.mmp.request.shop.document.MiraklUploadShopDocumentsRequest;

class MiraklUpdateShopProperties extends AbstractMiraklShopSharedProperties {

    @Resource
    private UboService uboService;

    ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> updateShopPhotoTypeBuilder(List<Map<String, String>> rows) {
        ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> builder = ImmutableList.builder();
        rows.forEach(row -> {
            String uboValue = row.get("UBO");
            if (StringUtils.isNumeric(uboValue)) {
                //business shareholder ubo
                maxUbos = uboValue;
                int ubo = Integer.valueOf(maxUbos);
                Map<Integer, Map<String, String>> uboKeys = uboService.generateMiraklUboKeys(Integer.valueOf(maxUbos));
                builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.ID_NUMBER), UUID.randomUUID().toString()));
                builder.add(createAdditionalField("adyen-ubo" + maxUbos + "-photoidtype", row.get("photoIdType")));
            } else {
                builder.add(createAdditionalField("adyen-" + uboValue + "-idnumber", UUID.randomUUID().toString()));
                builder.add(createAdditionalField("adyen-" + uboValue + "-photoidtype", row.get("photoIdType")));
            }
        });
        return builder;
    }

    ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> addMiraklShopUbos(List<Map<String, String>> rows) {
        ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> builder = ImmutableList.builder();
        rows.forEach(row -> {
            maxUbos = row.get("maxUbos");
            Map<Integer, Map<String, String>> uboKeys = uboService.generateMiraklUboKeys(4);
            if (maxUbos != null) {
                /* adding more ubos to a shop is dictated by the number of UBOs to update (as defined in Cucumber table)
                 example:- 3 UBOs to add. (4 - 3) + 1 = 2
                 so the method will start at UBO 2 until 4*/
                int noOfUbos = Integer.valueOf(maxUbos);
                for (noOfUbos = (4 - noOfUbos) + 1; noOfUbos <= 4; noOfUbos++) {
                    builder.add(createAdditionalField(uboKeys.get(noOfUbos).get(UboService.CIVILITY), civility()));
                    builder.add(createAdditionalField(uboKeys.get(noOfUbos).get(UboService.FIRSTNAME), FAKER.name().firstName()));
                    builder.add(createAdditionalField(uboKeys.get(noOfUbos).get(UboService.LASTNAME), FAKER.name().lastName()));
                    builder.add(createAdditionalField(uboKeys.get(noOfUbos).get(UboService.EMAIL), email));
                }
            }
        });
        return builder;
    }

    ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> updateMiraklShopUbos(List<Map<String, String>> rows) {
        ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> builder = ImmutableList.builder();
        rows.forEach(row -> {
            maxUbos = row.get("UBO");
            int ubo = Integer.valueOf(maxUbos);
            Map<Integer, Map<String, String>> uboKeys = uboService.generateMiraklUboKeys(Integer.valueOf(maxUbos));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.FIRSTNAME), row.get("firstName")));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.LASTNAME), row.get("lastName")));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.EMAIL), "adyen-mirakl" + UUID.randomUUID() + "@mailtrap.com"));

        });
        return builder;
    }

    ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> addUBOToMiraklShop(List<Map<String, String>> rows) {
        ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> builder = ImmutableList.builder();
        rows.forEach(row -> {
            maxUbos = row.get("UBO");
            int ubo = Integer.valueOf(maxUbos);
            Map<Integer, Map<String, String>> uboKeys = uboService.generateMiraklUboKeys(Integer.valueOf(maxUbos));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.CIVILITY), civility()));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.FIRSTNAME), FAKER.name().firstName()));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.LASTNAME), FAKER.name().lastName()));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.EMAIL), "adyen-mirakl" + UUID.randomUUID() + "@mailtrap.com"));

        });
        return builder;
    }

    ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> updateMiraklShopUbosWithInvalidData(List<Map<String, String>> rows) {
        ImmutableList.Builder<MiraklSimpleRequestAdditionalFieldValue> builder = ImmutableList.builder();
        rows.forEach(row -> {
            maxUbos = row.get("UBO");
            int ubo = Integer.valueOf(maxUbos);
            Map<Integer, Map<String, String>> uboKeys = uboService.generateMiraklUboKeys(Integer.valueOf(maxUbos));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.FIRSTNAME), UUID.randomUUID().toString()));
            builder.add(createAdditionalField(uboKeys.get(ubo).get(UboService.LASTNAME), UUID.randomUUID().toString()));
        });
        return builder;
    }

    MiraklProfessionalInformation updateMiraklShopTaxId(MiraklShop miraklShop) {
        MiraklProfessionalInformation miraklProfessionalInformation = new MiraklProfessionalInformation();
        miraklProfessionalInformation.setTaxIdentificationNumber("GB" + RandomStringUtils.randomNumeric(9));
        miraklProfessionalInformation.setIdentificationNumber(miraklShop.getProfessionalInformation().getIdentificationNumber());
        miraklProfessionalInformation.setCorporateName(miraklShop.getProfessionalInformation().getCorporateName());
        return miraklProfessionalInformation;
    }

    MiraklUploadShopDocumentsRequest uploadMiraklShopWithBankStatement(String shopId) {
        ImmutableList.Builder<MiraklUploadDocument> docUploadRequestBuilder = new ImmutableList.Builder<>();

        URL url = Resources.getResource("fileuploads/BankStatement.png");

        MiraklUploadDocument element = new MiraklUploadDocument();
        element.setFile(new File(url.getPath()));
        element.setFileName("BankStatement.png");
        element.setTypeCode("adyen-bankproof");

        docUploadRequestBuilder.add(element);

        return miraklUploadShopDocumentsRequest(shopId, docUploadRequestBuilder.build());
    }

    MiraklUploadShopDocumentsRequest uploadMiraklShopWithIdentityDoc(String shopId, List<Map<String, String>> rows) {
        ImmutableList.Builder<MiraklUploadDocument> docUploadRequestBuilder = new ImmutableList.Builder<>();
        rows.forEach(row -> {
            String ubo = row.get("UBO");
            if(StringUtils.isNumeric(ubo)) {
                //business shareholder ubo
                ubo = "ubo" + ubo;
            }

            // upload back documents only
            if (row.get("front").isEmpty()) {
                setFileUploadBackDocument(docUploadRequestBuilder, row, ubo);
            }
            // upload front documents only
            else if (row.get("back").isEmpty()) {
                setFileUploadFrontDocument(docUploadRequestBuilder, row, ubo);
            }
            // upload front and back documents
            else {
                setFileUploadFrontDocument(docUploadRequestBuilder, row, ubo);
                setFileUploadBackDocument(docUploadRequestBuilder, row, ubo);
            }

        });
        return miraklUploadShopDocumentsRequest(shopId, docUploadRequestBuilder.build());
    }

    private void setFileUploadBackDocument(ImmutableList.Builder<MiraklUploadDocument> builder, Map<String, String> row, String ubo) {
        MiraklUploadDocument element = new MiraklUploadDocument();
        URL url = Resources.getResource("fileuploads/" + row.get("back"));
        element.setFile(new File(url.getPath()));
        element.setFileName(row.get("back"));
        element.setTypeCode("adyen-" + ubo + "-photoid-rear");
        builder.add(element);
    }

    private void setFileUploadFrontDocument(ImmutableList.Builder<MiraklUploadDocument> builder, Map<String, String> row, String ubo) {
        MiraklUploadDocument element = new MiraklUploadDocument();
        URL url = Resources.getResource("fileuploads/" + row.get("front"));
        element.setFile(new File(url.getPath()));
        element.setFileName(row.get("front"));
        element.setTypeCode("adyen-" + ubo + "-photoid");
        builder.add(element);
    }

    MiraklIbanBankAccountInformation updateNewMiraklIbanOnly(MiraklShop miraklShop, List<Map<String, String>> rows) {
        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();
        MiraklPaymentInformation miraklPaymentInformation = miraklShop.getPaymentInformation();
        if (miraklPaymentInformation instanceof MiraklIbanBankAccountInformation) {
            paymentInformation.setIban(rows.get(0).get("iban"));
            paymentInformation.setBic(((MiraklIbanBankAccountInformation) miraklPaymentInformation).getBic());
            paymentInformation.setOwner(miraklPaymentInformation.getOwner());
            paymentInformation.setBankName(((MiraklIbanBankAccountInformation) miraklPaymentInformation).getBankName());
            paymentInformation.setBankCity(((MiraklIbanBankAccountInformation) miraklPaymentInformation).getBankCity());
        }
        return paymentInformation;
    }

    MiraklAbaBankAccountInformation updateMiraklBankAccountNumberOnly(MiraklShop miraklShop, List<Map<String, String>> rows) {
        MiraklAbaBankAccountInformation paymentInformation = new MiraklAbaBankAccountInformation();
        MiraklPaymentInformation miraklPaymentInformation = miraklShop.getPaymentInformation();
        if (miraklPaymentInformation instanceof MiraklAbaBankAccountInformation) {
            paymentInformation.setBankAccountNumber(rows.get(0).get("bankAccountNumber"));
            paymentInformation.setOwner(miraklPaymentInformation.getOwner());
            paymentInformation.setBankCity(((MiraklAbaBankAccountInformation) miraklPaymentInformation).getBankCity());
            paymentInformation.setBankZip(((MiraklAbaBankAccountInformation) miraklPaymentInformation).getBankZip());
            paymentInformation.setBankStreet(((MiraklAbaBankAccountInformation) miraklPaymentInformation).getBankStreet());
            paymentInformation.setBankName(((MiraklAbaBankAccountInformation) miraklPaymentInformation).getBankName());
            paymentInformation.setRoutingNumber(((MiraklAbaBankAccountInformation) miraklPaymentInformation).getRoutingNumber());
        }
        return paymentInformation;
    }

    MiraklShopAddress updateMiraklShopAddress(MiraklShop miraklShop, Map<String, String> row) {
        MiraklShopAddress address = new MiraklShopAddress();
        if (row.get("firstName") != null) {
            address.setCity(row.get("city"));
            address.setCivility(miraklShop.getContactInformation().getCivility());
            address.setCountry(miraklShop.getContactInformation().getCountry());
            address.setFirstname(row.get("firstName"));
            address.setLastname(row.get("lastName"));
            address.setStreet1(miraklShop.getContactInformation().getStreet1());
            address.setZipCode(row.get("postCode"));
        } else {
            address.setCity(row.get("city"));
            address.setCivility(miraklShop.getContactInformation().getCivility());
            address.setCountry(miraklShop.getContactInformation().getCountry());
            address.setFirstname(miraklShop.getContactInformation().getFirstname());
            address.setLastname(row.get("lastName"));
            address.setStreet1(miraklShop.getContactInformation().getStreet1());
            address.setZipCode(miraklShop.getContactInformation().getZipCode());
        }
        return address;
    }

    MiraklShopAddress updateMiraklShopFirstLineAddress(MiraklShop miraklShop) {
        MiraklShopAddress address = new MiraklShopAddress();
        address.setFirstname(miraklShop.getContactInformation().getFirstname());
        address.setCity(miraklShop.getContactInformation().getCity());
        address.setCivility(miraklShop.getContactInformation().getCivility());
        address.setCountry(miraklShop.getContactInformation().getCountry());
        address.setLastname(miraklShop.getContactInformation().getLastname());
        address.setStreet1(UUID.randomUUID().toString());
        address.setZipCode(miraklShop.getContactInformation().getZipCode());
        return address;
    }

    MiraklShopAddress udpateMiraklShopCity(MiraklShop miraklShop, Map<String, String> row) {
        MiraklShopAddress address = new MiraklShopAddress();
        address.setFirstname(miraklShop.getContactInformation().getFirstname());
        address.setCity(row.get("city"));
        address.setCivility(miraklShop.getContactInformation().getCivility());
        address.setCountry(miraklShop.getContactInformation().getCountry());
        address.setLastname(miraklShop.getContactInformation().getLastname());
        address.setStreet1(miraklShop.getContactInformation().getStreet1());
        address.setZipCode(miraklShop.getContactInformation().getZipCode());
        return address;
    }

    // Mandatory for shop update
    void populateMiraklShopPremiumSuspendAndPaymentBlockedStatus(MiraklShop miraklShop, MiraklUpdateShop miraklUpdateShop) {

        // will keep setSuspend false unless returned enum = SUSPEND
        boolean setSuspend;
        MiraklShopState state = miraklShop.getState();
        setSuspend = state.equals(MiraklShopState.SUSPENDED);
        miraklUpdateShop.setSuspend(setSuspend);
        miraklUpdateShop.setProfessional(miraklShop.isProfessional());
        miraklUpdateShop.setPaymentBlocked(miraklShop.getPaymentDetail().getPaymentBlocked());
        miraklUpdateShop.setPremiumState(miraklShop.getPremiumState());
    }

    void populateMiraklChannel(MiraklShop miraklShop, MiraklUpdateShop miraklUpdateShop) {
        // gets the sales channel code, if multiple found then immutable list should handle
        ImmutableList.Builder<String> channelsBuilder = new ImmutableList.Builder<>();
        for (String channel : miraklShop.getChannels()) {
            channelsBuilder.add(channel);
        }
        miraklUpdateShop.setChannels(channelsBuilder.build());
    }

    // Mandatory for shop update
    void populateShopNameAndEmail(MiraklShop miraklShop, MiraklUpdateShop miraklUpdateShop) {
        miraklUpdateShop.setName(miraklShop.getName());
        miraklUpdateShop.setEmail(miraklShop.getContactInformation().getEmail());
    }

    // Mandatory for shop update
    MiraklShopAddress populateMiraklShopAddress(MiraklShop miraklShop) {
        MiraklShopAddress address = new MiraklShopAddress();

        address.setCity(miraklShop.getContactInformation().getCity());
        address.setCivility(miraklShop.getContactInformation().getCivility());
        address.setCountry(miraklShop.getContactInformation().getCountry());
        address.setFirstname(miraklShop.getContactInformation().getFirstname());
        address.setLastname(miraklShop.getContactInformation().getLastname());
        address.setStreet1(miraklShop.getContactInformation().getStreet1());
        address.setZipCode(miraklShop.getContactInformation().getZipCode());

        return address;
    }

    // Mandatory for shop update
    MiraklShopAddress populateMiraklShopAddressForUS(MiraklShop miraklShop) {
        MiraklShopAddress address = new MiraklShopAddress();
        address.setCity("PASSED");
        address.setCivility(civility());
        address.setCountry("USA");
        address.setState("CA");
        address.setFirstname(FAKERUS.name().firstName());
        address.setLastname(FAKERUS.name().lastName());
        address.setStreet1(FAKERUS.address().streetAddress());
        address.setZipCode(FAKERUS.address().zipCodeByState("CA"));

        return address;
    }

    // Mandatory for shop update
    MiraklIbanBankAccountInformation populateMiraklIbanBankAccountInformation(MiraklShop miraklShop) {
        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();

        //update requires bank details for some reason
        if (miraklShop.getPaymentInformation() != null) {
            if (miraklShop.getPaymentInformation() instanceof MiraklIbanBankAccountInformation) {
                MiraklIbanBankAccountInformation ibanBankAccountInformation = (MiraklIbanBankAccountInformation) miraklShop.getPaymentInformation();
                paymentInformation.setIban(ibanBankAccountInformation.getIban());
                paymentInformation.setBic(ibanBankAccountInformation.getBic());
                paymentInformation.setOwner(ibanBankAccountInformation.getOwner());
                paymentInformation.setBankName(ibanBankAccountInformation.getBankName());
                paymentInformation.setBankCity(ibanBankAccountInformation.getBankCity());
            }
        } else {
            paymentInformation.setIban("GB26TEST40051512347366");
            paymentInformation.setBic(FAKER.finance().bic());
            paymentInformation.setOwner(FAKER.name().firstName() + " " + FAKER.name().lastName());
            paymentInformation.setBankName("RBS");
            paymentInformation.setBankCity("PASSED");
        }
        return paymentInformation;
    }

    MiraklAbaBankAccountInformation populateMiraklBankAccountInformationForUS(MiraklShop miraklShop) {
        MiraklAbaBankAccountInformation paymentInformation = new MiraklAbaBankAccountInformation();

        if (miraklShop.getPaymentInformation() != null) {
            if (miraklShop.getPaymentInformation() instanceof MiraklAbaBankAccountInformation) {
                MiraklAbaBankAccountInformation abaBankAccountInformation = (MiraklAbaBankAccountInformation) miraklShop.getPaymentInformation();
                paymentInformation.setBankAccountNumber(abaBankAccountInformation.getBankAccountNumber());
                paymentInformation.setOwner(abaBankAccountInformation.getOwner());
                paymentInformation.setBankName(abaBankAccountInformation.getBankName());
                paymentInformation.setBankCity(abaBankAccountInformation.getBankCity());
                paymentInformation.setRoutingNumber(abaBankAccountInformation.getRoutingNumber());
            }
        } else {
            paymentInformation.setBankAccountNumber("123456789");
            paymentInformation.setOwner(FAKER.name().firstName() + " " + FAKER.name().lastName());
            paymentInformation.setBankName("RBS");
            paymentInformation.setBankCity("PASSED");
            paymentInformation.setRoutingNumber("121000358");
        }
        return paymentInformation;
    }

    void populateMiraklAdditionalFields(MiraklUpdateShop miraklUpdateShop, MiraklShop miraklShop, ImmutableList<MiraklSimpleRequestAdditionalFieldValue> fieldsToUpdate) {

        final List<MiraklAdditionalFieldValue> addFields = new LinkedList<>(miraklShop.getAdditionalFieldValues());
        final ImmutableList.Builder<MiraklRequestAdditionalFieldValue> updatedFields = new ImmutableList.Builder<>();

        for (MiraklSimpleRequestAdditionalFieldValue additionalFieldVal : fieldsToUpdate) {
            Optional<MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue> additionalField = addFields.stream()
                                                                                                             .filter(x -> additionalFieldVal.getCode().equals(x.getCode()))
                                                                                                             .filter(MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue.class::isInstance)
                                                                                                             .map(MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue.class::cast)
                                                                                                             .findAny();

            // if fields are present then update them
            // else create them
            if (additionalField.isPresent()) {
                MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue field = additionalField.get();
                field.setValue(additionalFieldVal.getValue());
            } else {
                updatedFields.add(additionalFieldVal);
            }
        }

        // update patch

        final List<MiraklRequestAdditionalFieldValue> patchedUpdatedFields = new LinkedList<>();
        for (MiraklAdditionalFieldValue field : addFields) {
            if (field instanceof MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithMultipleValues) {
                patchedUpdatedFields.add(new MiraklRequestAdditionalFieldValue.MiraklMultipleRequestAdditionalFieldValue(field.getCode(),
                                                                                                                         ((MiraklAdditionalFieldValue
                                                                                                                                 .MiraklAbstractAdditionalFieldWithMultipleValues) field)
                                                                                                                                 .getValues()));
            } else if (field instanceof MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue) {
                patchedUpdatedFields.add(new MiraklSimpleRequestAdditionalFieldValue(field.getCode(), ((MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue) field).getValue()));
            } else {
                Assertions.fail("unexpected additional field type {0} ", field.getClass());
            }
        }
        updatedFields.addAll(patchedUpdatedFields);
        miraklUpdateShop.setAdditionalFieldValues(updatedFields.build());
    }

    void throwErrorIfShopFailedToUpdate(MiraklUpdatedShops miraklUpdatedShopsResponse) {
        final List<Set<ErrorBean>> errors = miraklUpdatedShopsResponse.getShopReturns()
                                                                      .stream()
                                                                      .map(MiraklUpdatedShopReturn::getShopError)
                                                                      .filter(Objects::nonNull)
                                                                      .map(InputWithErrors::getErrors)
                                                                      .collect(Collectors.toList());

        Assertions.assertThat(errors.size()).withFailMessage("errors on update: " + GSON.toJson(errors)).isZero();
    }

    void throwDocumentUploadError(MiraklDocumentsUploadResult uploadResult) {
        List<Set<ErrorBean>> errors = uploadResult.getDocuments().stream().map(InputWithErrors::getErrors).collect(Collectors.toList());
        Assertions.assertThat(errors.size()).withFailMessage("errors on update: " + GSON.toJson(errors)).isZero();
    }
}
