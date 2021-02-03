/**
 * 
 */
package io.mosip.registration.util.control.impl;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;

import io.mosip.commons.packet.dto.packet.SimpleDto;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.FXUtils;
import io.mosip.registration.controller.Initialization;
import io.mosip.registration.controller.VirtualKeyboard;
import io.mosip.registration.controller.reg.DemographicDetailController;
import io.mosip.registration.controller.reg.Validations;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.UiSchemaDTO;
import io.mosip.registration.util.common.DemographicChangeActionHandler;
import io.mosip.registration.util.control.FxControl;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * @author YASWANTH S
 *
 */
public class TextFieldFxControl extends FxControl {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(DemographicDetailController.class);

	private static String loggerClassName = " Text Field Control Type Class";

//	@Autowired
	private BaseController baseController;

//	@Autowired
//	@Autowired
	private ResourceLoader resourceLoader;
//	@Autowired
	private Validations validation;
//	@Autowired
	private DemographicChangeActionHandler demographicChangeActionHandler;

	public TextFieldFxControl() {

		ApplicationContext applicationContext = Initialization.getApplicationContext();
//		baseController = applicationContext.getBean(BaseController.class);
		this.demographicDetailController = applicationContext.getBean(DemographicDetailController.class);

//		resourceLoader = applicationContext.getBean(ResourceLoader.class);
		validation = applicationContext.getBean(Validations.class);
		demographicChangeActionHandler = applicationContext.getBean(DemographicChangeActionHandler.class);

	}

	@Override
	public FxControl build(UiSchemaDTO uiSchemaDTO) {
		this.uiSchemaDTO = uiSchemaDTO;

		this.control = this;

		VBox primaryLangVBox = create(uiSchemaDTO, "");

		HBox hBox = new HBox();
		hBox.setSpacing(20);
		hBox.getChildren().add(primaryLangVBox);

		if (demographicDetailController.isLocalLanguageAvailable()
				&& !demographicDetailController.isAppLangAndLocalLangSame()) {

			VBox secondaryLangVBox = create(uiSchemaDTO, RegistrationConstants.LOCAL_LANGUAGE);

			hBox.getChildren().add(secondaryLangVBox);

		}

		this.node = hBox;

		setListener((TextField) getField(RegistrationConstants.HASH + uiSchemaDTO.getId()));

		return this.control;
	}

	@Override
	public void copyTo(Node srcNode, List<Node> targetNodes) {

		// TODO Throw Reg Check based exception if src or target nodes were not present
		if (srcNode != null && targetNodes != null && !targetNodes.isEmpty()) {
			TextField srctextField = (TextField) srcNode;

			for (Node targetNode : targetNodes) {

				TextField targetTextField = (TextField) targetNode;
				targetTextField.setText(srctextField.getText());
			}
		}
	}

	@Override
	public void setData() {

		RegistrationDTO registrationDTO = getRegistrationDTo();

		if (this.uiSchemaDTO.getType().equalsIgnoreCase(RegistrationConstants.SIMPLE_TYPE)) {

			String primaryLang = null;
			String primaryVal = null;
			String localLanguage = null;
			String localVal = null;
			if (demographicDetailController.isLocalLanguageAvailable()
					&& !demographicDetailController.isAppLangAndLocalLangSame()) {

			}
			registrationDTO.addDemographicField(uiSchemaDTO.getId(), primaryLang, primaryVal, localLanguage, localVal);

		} else {
			registrationDTO.addDemographicField(uiSchemaDTO.getId(),
					((TextField) getField(uiSchemaDTO.getId())).getText());

		}
	}

	@Override
	public UiSchemaDTO getUiSchemaDTO() {

		return this.uiSchemaDTO;
	}

	@Override
	public void setListener(Node node) {
		FXUtils.getInstance().onTypeFocusUnfocusListener(getNode(), (TextField) node);

		TextField textField = (TextField) node;
		textField.addEventHandler(Event.ANY, event -> {
			if (isValid(textField)) {

				setData();

				// handling other handlers
				UiSchemaDTO uiSchemaDTO = validation.getValidationMap()
						.get(node.getId().replaceAll(RegistrationConstants.ON_TYPE, RegistrationConstants.EMPTY)
								.replaceAll(RegistrationConstants.LOCAL_LANGUAGE, RegistrationConstants.EMPTY));
				if (uiSchemaDTO != null) {
					LOGGER.info(loggerClassName, APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
							"Invoking external action handler for .... " + uiSchemaDTO.getId());
					demographicChangeActionHandler.actionHandle(getNode(), node.getId(), uiSchemaDTO.getChangeAction());
				}
				// Group level visibility listeners
				refreshFields();
			}
		});

	}

	private VBox create(UiSchemaDTO uiSchemaDTO, String languageType) {

		String fieldName = uiSchemaDTO.getId();

		// Get Mandatory Astrix
		String mandatorySuffix = getMandatorySuffix(uiSchemaDTO);

		/** Container holds title, fields and validation message elements */
		VBox simpleTypeVBox = new VBox();
		simpleTypeVBox.setId(fieldName + languageType + RegistrationConstants.VBOX);
		simpleTypeVBox.setSpacing(5);

		String titleText = (languageType.equals(RegistrationConstants.LOCAL_LANGUAGE)
				? uiSchemaDTO.getLabel().get(RegistrationConstants.SECONDARY)
				: uiSchemaDTO.getLabel().get(RegistrationConstants.PRIMARY)) + mandatorySuffix;

		double prefWidth = simpleTypeVBox.getPrefWidth();

		/** Title label */
		Label fieldTitle = getLabel(fieldName + languageType + RegistrationConstants.LABEL, titleText,
				RegistrationConstants.DEMOGRAPHIC_FIELD_LABEL, true, prefWidth);
		simpleTypeVBox.getChildren().add(fieldTitle);

		/** Text Field */
		TextField textField = getTextField(fieldName + languageType, titleText,
				RegistrationConstants.DEMOGRAPHIC_TEXTFIELD, prefWidth,
				languageType.equals(RegistrationConstants.LOCAL_LANGUAGE)
						&& !uiSchemaDTO.getType().equals(RegistrationConstants.SIMPLE_TYPE) ? true : false);
		simpleTypeVBox.getChildren().add(textField);

		/** Validation message (Invalid/wrong,,etc,.) */
		Label validationMessage = getLabel(fieldName + languageType + RegistrationConstants.MESSAGE, null,
				RegistrationConstants.DemoGraphicFieldMessageLabel, false, prefWidth);
		simpleTypeVBox.getChildren().add(validationMessage);

		if (languageType.equals(RegistrationConstants.LOCAL_LANGUAGE) && !textField.isDisable()) {
			// If Local Language and simpleType : Set KeyBoard

			addKeyBoard(simpleTypeVBox, validationMessage, textField);

		}

		return simpleTypeVBox;
	}

	private void addKeyBoard(VBox simpleTypeVBox, Label validationMessage, TextField textField) {
		ImageView keyBoardImgView = getKeyBoardImage();

		if (keyBoardImgView != null) {
			keyBoardImgView.setOnMouseClicked((event) -> {
				demographicDetailController.setFocusonLocalField(event);
			});

			VirtualKeyboard keyBoard = VirtualKeyboard.getInstance();
			keyBoard.view();
			keyBoard.changeControlOfKeyboard(textField);

			HBox keyBoardHBox = new HBox();
			keyBoardHBox.setSpacing(20);
			keyBoardHBox.getChildren().add(keyBoardImgView);
			keyBoardHBox.getChildren().add(validationMessage);
			keyBoardHBox.setStyle("-fx-background-color:WHITE");
			simpleTypeVBox.getChildren().add(keyBoardHBox);
		}
	}

	private TextField getTextField(String id, String titleText, String demographicTextfield, double prefWidth,
			boolean isDisable) {

		/** Text Field */
		TextField textField = new TextField();
		textField.setId(id);
		textField.setPromptText(titleText);
		textField.getStyleClass().add(RegistrationConstants.DEMOGRAPHIC_TEXTFIELD);
		textField.setPrefWidth(prefWidth);
		textField.setDisable(isDisable);

		return textField;
	}

	private ImageView getKeyBoardImage() {
		ImageView imageView = null;

		imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/keyboard.png")));
		imageView.setId(uiSchemaDTO.getId());
		imageView.setFitHeight(20.00);
		imageView.setFitWidth(22.00);

		return imageView;
	}

	@Override
	public Object getData() {

		return getRegistrationDTo().getDemographics().get(uiSchemaDTO.getId());
	}

	@Override
	public boolean isValid(Node node) {

		boolean isValid;
		if (node == null) {
			LOGGER.warn(loggerClassName, APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					"Field not found in demographic screen");
			return false;
		}

		TextField field = (TextField) node;
		if (validation.validateTextField(getNode(), field, field.getId(), true)) {

			FXUtils.getInstance().setTextValidLabel(getNode(), field);
			isValid = true;
		} else {

			FXUtils.getInstance().showErrorLabel(field, getNode());
			return false;
		}
		LOGGER.debug(loggerClassName, APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
				"validating text field secondary");

		TextField localField = (TextField) getField(uiSchemaDTO.getId() + RegistrationConstants.LOCAL_LANGUAGE);
		if (localField != null) {
			if (validation.validateTextField(getNode(), field, field.getId(), true)) {

				FXUtils.getInstance().setTextValidLabel(getNode(), localField);
				isValid = true;
			} else {

				FXUtils.getInstance().showErrorLabel(localField, getNode());
				return false;
			}
		}
		return isValid;

	}

	@Override
	public HBox getNode() {
		return (HBox) this.node;
	}

}