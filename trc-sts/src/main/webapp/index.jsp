<!DOCTYPE html>
<html lang="en" class="container-strech-smaller-1500">
<head>
    <title>eHDSI OpenNCP | TRC-STS</title>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

    <!--- CSS LIBRARY --->
    <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="vendor/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" href="vendor/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css">
    <link rel="stylesheet" href="vendor/bootstrap-select/css/bootstrap-select.min.css">
    <link rel="stylesheet" href="vendor/flag-icon-svg/css/flag-icon.min.css">

    <!--- CSS UI --->
    <link href="css/ui/grid.css" rel="stylesheet">
    <link rel="stylesheet" href="css/ui/section.css">
    <link rel="stylesheet" href="css/ui/boxes.css">
    <link rel="stylesheet" href="css/ui/button.css">
    <link rel="stylesheet" href="css/ui/form.css">
    <link rel="stylesheet" href="css/ui/label.css">
    <link rel="stylesheet" href="css/ui/panel.css">
    <link rel="stylesheet" href="css/ui/table.css">
    <link rel="stylesheet" href="css/ui/table-like.css">
    <link rel="stylesheet" href="css/ui/datatable.css">
    <link rel="stylesheet" href="css/ui/pagination.css">
    <link rel="stylesheet" href="css/ui/page-header.css">
    <link rel="stylesheet" href="css/ui/treeview.css">
    <link rel="stylesheet" href="css/ui/modal.css">
    <link rel="stylesheet" href="css/ui/dropdown.css">
    <link rel="stylesheet" href="css/ui/nav.css">
    <link rel="stylesheet" href="css/ui/alert.css">
    <link rel="stylesheet" href="css/ui/legend.css">
    <link rel="stylesheet" href="css/ui/tooltip.css">
    <link rel="stylesheet" href="css/ui/popover.css">
    <link rel="stylesheet" href="css/ui/helpers.css">
    <link rel="stylesheet" href="css/ui/hr.css">
    <link rel="stylesheet" href="css/ui/process-step.css">

    <!--- CSS APP --->
    <link rel="stylesheet" href="css/app/ec_banner.css">
    <link rel="stylesheet" href="css/app/sidebar.css">
    <link rel="stylesheet" href="css/app/main_nav.css">
    <link rel="stylesheet" href="css/app/main_footer.css">
    <link rel="stylesheet" href="css/app/app.css">
</head>

<body>
    <div id="main-wrapper" class="container">
        <nav class="sr-only">
            <ul>
                <li><a href="#main-nav">Go to menu</a></li>
                <li><a href="#main-content">Go to content</a></li>
            </ul>
        </nav>
        <div id="ec-banner">
            <div id="ec-banner-inner" class="clearfix">
                <nav id="ec-user-nav" class="hidden-print">
                    <ul class="nav nav-pills">
                        <li class="nav-item">
                            <a href="mailto:dont-reply@ec.europa.eu">
                                <i class="glyphicon glyphicon-envelope"></i>
                                <span class="nav-item-title">Support</span>
                            </a>
                        </li>
                    </ul>
                </nav>
                <img id="ec-logo" src="vendor/ec/css/images/logo/logo_en.gif" width="172" height="119"
                     alt="european commission logo">
                <h1 id="ec-title">
                    <strong>DG SANTE</strong>
                    <span>eHDSI OpenNCP TRC-STS</span>
                </h1>
            </div>
        </div>
        <nav id="ec-banner-path" role="navigation" class="hidden-print hidden-xs">
            <p class="sr-only" id="breadcrumblabel">Navigation path</p>
            <ul class="reset-list" aria-labelledby="breadcrumblabel">
                <li class="first">
                    <a href="http://ec.europa.eu/index_en.htm" target="_blank">European Commission</a>
                </li>
                <li>
                    <a href="https://ec.europa.eu/cefdigital/wiki/x/8CEZAg">CEF eHDSI OpenNCP</a>
                </li>
            </ul>
        </nav>
        <nav id="main-nav" class="navbar navbar-default navbar-divider hidden-print" role="navigation">
        </nav>
        <div id="main-content" role="main">
            <header class="page-header">
                <h2>Treatment Relationship Care - Security Token Services</h2>
            </header>
            <p>&nbsp;</p>
            <p>The OpenNCP Confirmation Assertion is encoded as a SAML 2.0 assertion. Every Treatment Relationship
                Confirmation Assertion MUST be signed by its issuer. For the TRC assertion, it MAY be the NCP-B.
                The XML signature MUST be applied by using the saml:Assertion/ds:Signature</p>
            <p>&nbsp;</p>
            <a href="STSServiceService">TRC-STS Service</a>
            <p>&nbsp;</p>
            <hr class="hr"/>
        </div>
    </div>

    <footer id="main-footer">
        <p>Version 0.1</p>
        <div data-spy="affix" data-offset-top="200" title="Scroll top" class="scroll-to-top affix hidden-print">
            <a href="#main-nav"><i class="glyphicon glyphicon-menu-up"></i></a>
        </div>
    </footer>

    <script src="js/jquery/jquery-latest.min.js"></script>
    <script src="js/moment/moment.min.js"></script>
    <script src="vendor/bootstrap/js/bootstrap.min.js"></script>
    <script src="js/jquery/plugins/datatable/datatables.min.js"></script>
    <script src="vendor/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
    <script src="vendor/bootstrap-select/js/bootstrap-select.min.js"></script>
    <script src="vendor/bootstrap-typeahead/bootstrap3-typeahead.min.js"></script>
    </body>
</html>
