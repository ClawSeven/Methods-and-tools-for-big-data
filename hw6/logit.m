load hospital
ds = hospital;
modelspec = 'Smoker ~Age*Weight*Sex - Age:Weight:Sex';
% A*B*C means A, B, C, A*B, A*C, B*C, A*B*C
% - Y   means do not include Y
% A:B	means A*B only

mdl = fitglm (ds , modelspec , 'Distribution', 'binomial');

dsSmoke = hospital(hospital.Smoker==1,:);
dsNoSmoke = hospital(hospital.Smoker==0,:);

hold on
scatter(dsSmoke.Age, dsSmoke.Weight, 'r');
scatter(dsNoSmoke.Age, dsNoSmoke.Weight, 'b');

pred = ds(3, :);
pred.Age(1) = 20;
pred.Weight(1) = 130;
predict(mdl, pred);
