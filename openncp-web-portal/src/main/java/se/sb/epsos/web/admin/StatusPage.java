package se.sb.epsos.web.admin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;
import se.sb.epsos.web.BasePage;
import se.sb.epsos.web.EpsosWebApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author danielgronberg
 */
@AuthorizeInstantiation({"ROLE_ADMIN"})
public class StatusPage extends BasePage {
    private static final int HOLDBACK_IN_SECONDS = 3;
    private static final int REFRESHTIME_IN_SECONDS = 15 * 60;

    LoadableDetachableModel<List<NcpStatusInterface>> loadableBeans = new LoadableDetachableModel<List<NcpStatusInterface>>() {
        private static final long serialVersionUID = -5969322729613746031L;
        List<NcpStatusInterface> beanList = new ArrayList<>();

        @Override
        protected List<NcpStatusInterface> load() {
            Map<String, NcpStatusInterface> beanMap = ((EpsosWebApplication) getApplication()).getApplicationContext().getBeansOfType(
                    NcpStatusInterface.class);
            beanList.addAll(beanMap.values());
            return beanList;
        }
    };

    public StatusPage() {
        add(new ListView<NcpStatusInterface>("statusList", loadableBeans.getObject()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final ListItem<NcpStatusInterface> item) {
                item.setOutputMarkupId(true);
                final NcpStatusInterface statusBean = item.getModel().getObject();
                Label titleLabel = new Label("title", statusBean.getTitle());

                item.add(titleLabel);

                titleLabel.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(HOLDBACK_IN_SECONDS)) {
                    private static final long serialVersionUID = -4844556010799671141L;

                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {

                        Boolean status = statusBean.getStatus();
                        if (status) {
                            target.appendJavascript("highlightSuccess('" + item.getMarkupId() + "')");
                        } else {
                            target.appendJavascript("highlightFailure('" + item.getMarkupId() + "')");
                        }
                        target.addComponent(item);
                        super.setUpdateInterval(Duration.seconds(REFRESHTIME_IN_SECONDS));
                        super.onPostProcessTarget(target);
                    }
                });
            }
        });
        add(new AbstractBehavior() {
            private static final long serialVersionUID = -2421262991757111069L;

            @Override
            public void renderHead(IHeaderResponse response) {
                response.renderOnLoadJavascript("animateYellow()");
            }
        });
    }
}
