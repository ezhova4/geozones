package com.clevertec.datatransfer.front;

import com.clevertec.datatransfer.dto.ZoneInfo;
import com.clevertec.datatransfer.entity.ZoneParentInfo;
import com.clevertec.datatransfer.exception.ErrorResponseFromWialonException;
import com.clevertec.datatransfer.service.GeoZoneService;
import com.clevertec.datatransfer.service.ParsingService;
import com.helger.commons.csv.CSVWriter;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Route
public class MainView extends VerticalLayout implements HasUrlParameter<String> {
    @Autowired
    private ParsingService parsingService;
    @Autowired
    private GeoZoneService geoZoneService;
    private String sid;

    private Dialog dialog = new Dialog();
    private Grid<ZoneInfo> grid = new Grid<>();
    private MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private Upload upload = new Upload(buffer);
    private List<ZoneParentInfo> selectedItemIds = new ArrayList<>();
    private Button deleteButton = new Button();
    private List<ZoneInfo> zoneInfoList;
    private Button closeNotificationButton = new Button("Close");
    private Notification notification = new Notification();
    private Label notificationLabel = new Label();
    private Anchor anchor;
    private ListDataProvider<ZoneInfo> dataProvider;
    private HeaderRow filterRow;
    private Grid.Column<ZoneInfo> titleColumn;
    private Grid.Column<ZoneInfo> descriptionColumn;
    private Grid.Column<ZoneInfo> parentColumn;

    public MainView(GeoZoneService geoZoneService) {
        this.geoZoneService = geoZoneService;
        setWidth("99%");
        setHeight("99%");

        closeNotificationButton.addClickListener(event -> notification.close());
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.add(notificationLabel);
        notification.add(closeNotificationButton);

        configUpload();
        configDeleteButton();
        configAnchor();

        add(
                new HorizontalLayout(
                        upload,
                        anchor
                ),
                grid,
                deleteButton
        );
    }

    private void configDeleteButton() {
        deleteButton.setText("Удалить");
        deleteButton.setIcon(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setEnabled(false);
        deleteButton.addClickListener(event -> {
            deleteZones();
            zoneInfoList = getZoneInfoList();
            selectedItemIds.clear();
            grid.asMultiSelect().deselectAll();
            configFilters();
        });
    }

    private void configUpload() {
        upload.setI18n(getUploadI18N());
        upload.setAcceptedFileTypes(".csv");
        upload.setDropAllowed(false);
        upload.setHeight("30px");
        upload.addFinishedListener(event -> {
            zoneInfoList = createZonesAndGetAllZoneList(buffer.getInputStream(event.getFileName()));
            configFilters();
        });
    }

    private void configGrid() {
        grid.addColumn(ZoneInfo::getId).setHeader("Id").setWidth("10%");
        titleColumn = grid.addColumn(ZoneInfo::getTitle).setHeader("Название").setWidth("120px");
        descriptionColumn = grid.addColumn(ZoneInfo::getDescription).setHeader("Описание").setWidth("120px");
        parentColumn = grid.addColumn(ZoneInfo::getParent).setHeader(new Html("<p>Родитель<br>зоны</p>")).setWidth("100px");
        grid.addColumn(ZoneInfo::getRadius).setHeader("Радиус").setWidth("100px");
        grid.addColumn(ZoneInfo::getLatitude).setHeader("Широта").setWidth("110px");
        grid.addColumn(ZoneInfo::getLongitude).setHeader("Долгота").setWidth("110px");
        grid.addColumn(ZoneInfo::getZoneColor).setHeader("Цвет зоны").setVisible(false);
        grid.addColumn(ZoneInfo::getSignatureColor).setHeader("Цвет подписи").setVisible(false);
        grid.addColumn(ZoneInfo::getFontHeight).setHeader("Высота шрифта").setVisible(false);
        grid.addColumn(ZoneInfo::getScale).setHeader("Видимость").setVisible(false);
        grid.addComponentColumn(this::createImageButton).setHeader("Изображение").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.getColumns().forEach(zoneInfoColumn -> zoneInfoColumn
                .setResizable(true)
                .setSortable(true)
                .setFlexGrow(1)
        );
        filterRow = grid.appendHeaderRow();
        configFilters();

        grid.asMultiSelect().addValueChangeListener(event -> {
            Set<ZoneInfo> zoneInfoSet = event.getValue();
            selectedItemIds = zoneInfoSet.stream()
                    .map(zoneInfo -> new ZoneParentInfo(zoneInfo.getId(), zoneInfo.getParent()))
                    .collect(Collectors.toList());
            deleteButton.setEnabled(selectedItemIds.size() > 0);
        });
    }

    private void configFilters() {
        dataProvider = new ListDataProvider<>(zoneInfoList);
        grid.setDataProvider(dataProvider);
        // Title filter
        TextField titleField = new TextField();
        titleField.addValueChangeListener(
                event -> dataProvider.addFilter(
                        zoneInfo -> StringUtils.containsIgnoreCase(zoneInfo.getTitle(), titleField.getValue())));
        configFilterColumn(titleColumn, filterRow, titleField);
        // Second filter
        TextField descriptionField = new TextField();
        descriptionField.addValueChangeListener(event -> dataProvider.addFilter(zoneInfo -> StringUtils.containsIgnoreCase(zoneInfo.getDescription(), descriptionField.getValue())));
        configFilterColumn(descriptionColumn, filterRow, descriptionField);
        // Third filter
        TextField parentField = new TextField();
        parentField.addValueChangeListener(event -> dataProvider.addFilter(zoneInfo -> StringUtils.containsIgnoreCase(zoneInfo.getParent(), parentField.getValue())));
        configFilterColumn(parentColumn, filterRow, parentField);
    }

    private void configFilterColumn(Grid.Column<ZoneInfo> column, HeaderRow filterRow, TextField field) {
        filterRow.getCell(column).setComponent(field);
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.setSizeFull();
        field.setPlaceholder("Filter");
    }

    private void configAnchor() {
        anchor = new Anchor(new StreamResource("geozones.csv", this::saveGeozonesToCSV), "");
        anchor.getElement().setAttribute("download", true);
        anchor.setHeight("36px");
        Button button = new Button("Сохранить как CSV", new Icon(VaadinIcon.DOWNLOAD_ALT));
        button.setSizeFull();
        anchor.add(button);
    }

    private InputStream saveGeozonesToCSV() {
        try {
            StringWriter stringWriter = new StringWriter();

            CSVWriter csvWriter = new CSVWriter(stringWriter);
            csvWriter.setSeparatorChar(';');
            csvWriter.setApplyQuotesToAll(false);
            csvWriter.writeNext("Id", "Название", "Описание", "Родитель Зоны",
                    "Радиус", "Широта", "Долгота", "Цвет зоны(HEX)", "Цвет подписи(HEX)",
                    "Высота шрифта", "Видимость", "Картинка");
            geoZoneService.getZonesInfo(geoZoneService.getZonesIds()).forEach(zone -> csvWriter.writeNext(
                    "" + StringUtils.substringAfterLast(zone.getId(), "_"),
                    zone.getTitle(),
                    zone.getDescription(),
                    zone.getParent(),
                    zone.getRadius(),
                    zone.getLatitude(),
                    zone.getLongitude(),
                    zone.getZoneColor(),
                    zone.getSignatureColor(),
                    "" + zone.getFontHeight(),
                    "" + zone.getScale(), ""));
            return IOUtils.toInputStream(stringWriter.toString(), Charset.forName("Cp1251"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Image getImage(String id) {
        return new Image(geoZoneService.getGeozoneImageUrl(id), "Изображение не найдено");
    }

    private Button createImageButton(ZoneInfo zoneInfo) {
        Button button = new Button();
        button.setText("Изображение");
        button.setIcon(VaadinIcon.PICTURE.create());
        button.setWidthFull();
        button.addClickListener(event -> {
            dialog.removeAll();
            dialog.add(getImage(zoneInfo.getId()));
            dialog.open();
        });
        return button;
    }

    private List<ZoneInfo> getZoneInfoList() {
        List<ZoneInfo> zonesInfo = new ArrayList<>();
        try {
            zonesInfo = geoZoneService.getZonesInfo(geoZoneService.getZonesIds());
        } catch (IOException | ErrorResponseFromWialonException e) {
            invokeNotification(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return zonesInfo;
    }

    private void deleteZones() {
        try {
            geoZoneService.deleteZones(selectedItemIds);
        } catch (IOException | ErrorResponseFromWialonException e) {
            invokeNotification(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private UploadI18N getUploadI18N() {
        UploadI18N i18n = new UploadI18N();
        i18n.setDropFiles(
                new UploadI18N.DropFiles().setOne("Перетащите файл сюда...")
                        .setMany("Перетащите файлы сюда..."))
                .setAddFiles(new UploadI18N.AddFiles()
                        .setOne("Выбрать файл").setMany("Добавить файлы"))
                .setCancel("Отменить")
                .setError(new UploadI18N.Error()
                        .setTooManyFiles("Слишком много файлов.")
                        .setFileIsTooBig("Слишком большой файл.")
                        .setIncorrectFileType("Некорректный тип файла."))
                .setUploading(new UploadI18N.Uploading()
                        .setStatus(new UploadI18N.Uploading.Status()
                                .setConnecting("Соединение...")
                                .setStalled("Загрузка застопорилась.")
                                .setProcessing("Обработка файла..."))
                        .setRemainingTime(
                                new UploadI18N.Uploading.RemainingTime()
                                        .setPrefix("оставшееся время: ")
                                        .setUnknown(
                                                "оставшееся время неизвестно"))
                        .setError(new UploadI18N.Uploading.Error()
                                .setServerUnavailable("Сервер недоступен")
                                .setUnexpectedServerError(
                                        "Неожиданная ошибка сервера")
                                .setForbidden("Загрузка запрещена")))
                .setUnits(Stream
                        .of("Б", "Кбайт", "Мбайт", "Гбайт", "Тбайт", "Пбайт",
                                "Эбайт", "Збайт", "Ибайт")
                        .collect(Collectors.toList()));
        return i18n;
    }

    private List<ZoneInfo> createZonesAndGetAllZoneList(InputStream buffer) {
        try {
            return geoZoneService.createGeoZones(parsingService.parse(buffer));
        } catch (ParseException | IOException | ErrorResponseFromWialonException e) {
            invokeNotification(e.getLocalizedMessage());
            e.printStackTrace();
            try {
                return geoZoneService.getZonesInfo(geoZoneService.getZonesIds());
            } catch (IOException | ErrorResponseFromWialonException ex) {
                invokeNotification(ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    private void invokeNotification(String localizedMessage) {
        notificationLabel.removeAll();
        notificationLabel.setText(localizedMessage);
        notification.open();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        sid = event.getLocation().getQueryParameters().getParameters().get("sid").get(0);
        log.info("SID: {}", sid);
        geoZoneService.setSession(sid);
        zoneInfoList = getZoneInfoList();
        configGrid();
    }
}
