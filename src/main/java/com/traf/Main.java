package com.traf;

import com.traf.core.Plan;
import com.traf.core.Subscription;
import com.traf.core.User;
import com.traf.db.SubscriptionDAO;
import com.traf.db.UserDAO;
import com.traf.db.PlanDAO;
import com.traf.service.UsageSyncTask;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class Main extends Application<AppConfiguration> {

    PlanDAO planDAO;
    UserDAO userDAO;
    SubscriptionDAO subscriptionDAO;

    private final MigrationsBundle<AppConfiguration> migrationsBundle = new MigrationsBundle<AppConfiguration>() {
        @Override
        public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    private final HibernateBundle<AppConfiguration> hibernateBundle =
            new HibernateBundle<>(Plan.class, User.class, Subscription.class) { // Add Plan.class here
                @Override
                public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    private final GuiceBundle guiceBundle = GuiceBundle.builder()
            .enableAutoConfig("com.traf")
            .modules(new SubscriptionModule(hibernateBundle)) // Temporary placeholders
            .printDiagnosticInfo()
            .build();

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.addBundle(migrationsBundle);
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) throws Exception {

//        final PLanDAO planDAO = new PLanDAO((hibernateBundle.getSessionFactory()));
//        final UserDAO userDAO = new UserDAO(hibernateBundle.getSessionFactory());
//        final SubscriptionDAO subscriptionDAO = new SubscriptionDAO(hibernateBundle.getSessionFactory());
//
//        final SubscriptionService subscriptionService = new SubscriptionService(
//                planDAO,
//                userDAO,
//                subscriptionDAO
//        );
//
//        environment.jersey().register(new HelloWorldResource());
//        environment.jersey().register(new SubscriptionResource(subscriptionService));
//        environment.jersey().register(new PlanResource(planDAO));
//        environment.jersey().register(new UserResource(userDAO));

//        UsageSyncTask syncTask = guiceBundle.getInjector().getInstance(UsageSyncTask.class);
//        environment.lifecycle().manage(syncTask);
    }

    public static void main(String[] args) throws Exception{
        new Main().run(args);
    }
}