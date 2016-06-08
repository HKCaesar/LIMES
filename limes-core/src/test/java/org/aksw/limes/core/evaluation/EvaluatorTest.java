/**
 *
 */
package org.aksw.limes.core.evaluation;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.limes.core.datastrutures.GoldStandard;
import org.aksw.limes.core.datastrutures.TaskAlgorithm;
import org.aksw.limes.core.datastrutures.TaskData;
import org.aksw.limes.core.evaluation.evaluationDataLoader.DataSetChooser;
import org.aksw.limes.core.evaluation.evaluationDataLoader.EvaluationData;
import org.aksw.limes.core.evaluation.evaluator.Evaluator;
import org.aksw.limes.core.evaluation.evaluator.EvaluatorType;
import org.aksw.limes.core.exceptions.UnsupportedMLImplementationException;
import org.aksw.limes.core.measures.measure.MeasureType;
import org.aksw.limes.core.ml.algorithm.AMLAlgorithm;
import org.aksw.limes.core.ml.algorithm.Eagle;
import org.aksw.limes.core.ml.algorithm.MLAlgorithmFactory;
import org.aksw.limes.core.ml.algorithm.MLImplementationType;
import org.aksw.limes.core.ml.algorithm.WombatSimple;
import org.aksw.limes.core.ml.setting.LearningParameter;
import org.junit.Test;

import com.google.common.collect.Table;


/**
 * @author mofeed
 */
@Deprecated
public class EvaluatorTest {
    private static final String PARAMETER_MAX_REFINEMENT_TREE_SIZE = "max refinement tree size";
    private static final String PARAMETER_MAX_ITERATIONS_NUMBER = "max iterations number";
    private static final String PARAMETER_MAX_ITERATION_TIME_IN_MINUTES = "max iteration time in minutes";
    private static final String PARAMETER_EXECUTION_TIME_IN_MINUTES = "max execution time in minutes";
    private static final String PARAMETER_MAX_FITNESS_THRESHOLD = "max fitness threshold";
    private static final String PARAMETER_MIN_PROPERTY_COVERAGE = "minimum properity coverage";
    private static final String PARAMETER_PROPERTY_LEARNING_RATE = "properity learning rate";
    private static final String PARAMETER_OVERALL_PENALTY_WEIT = "overall penalty weit";
    private static final String PARAMETER_CHILDREN_PENALTY_WEIT = "children penalty weit";
    private static final String PARAMETER_COMPLEXITY_PENALTY_WEIT = "complexity penalty weit";
    private static final String PARAMETER_VERBOSE = "verbose";
    private static final String PARAMETER_MEASURES = "measures";
    private static final String PARAMETER_SAVE_MAPPING = "save mapping";
    
    private static final int folds=5;
    private static final boolean crossValidate=false;

/////////////////////////////////////////////////////////////////////////////////////////    
    final public String[] datasetsList = {"PERSON1"/*, "PERSON1_CSV", "PERSON2", "PERSON2_CSV", "RESTAURANTS", "OAEI2014BOOKS"*/};
 //   final public String[] algorithmsList = {"SUPERVISED_ACTIVE:EAGLE", "SUPERVISED_BATCH:EAGLE", "UNSUPERVISED:EAGLE"};
    final public String[] algorithmsList = {"UNSUPERVISED:WOMBATSIMPLE"/*,"SUPERVISED_BATCH:WOMBATSIMPLE"*/};
/////////////////////////////////////////////////////////////////////////////////////////////////
    public Evaluator evaluator = new Evaluator();
    public List<TaskAlgorithm> mlAlgorithms = new ArrayList<TaskAlgorithm>();
    public Set<EvaluatorType> evaluators=new TreeSet<EvaluatorType>();
    public Set<TaskData> tasks =new TreeSet<TaskData>();

    @Test
    public void test() {
        if(!crossValidate)
            testEvaluator();
        else
            testCrossValidate();
    }

    /**
     * @param algorithms:
     *         the set of algorithms used to generate the predicted mappings
     * @param datasets:
     *         the set of dataets to apply the algorithms on them. The should include source Cache, target Cache, goldstandard and predicted mapping
     * @param QlMeasures:
     *         set of qualitative measures
     * @param QnMeasures;
     *         set of quantitative measures
     * @return table contains the results corresponding to the algorithms and measures (algorithm,measure,{measure,value})
     */
    public void testEvaluator() {
        try {
            
            initializeDataSets();
            initializeEvaluators();
            initializeMLAlgorithms();
           Table<String, String, Map<EvaluatorType, Double>> results = evaluator.evaluate(mlAlgorithms, tasks, evaluators, null);
           System.in.read();
           
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertTrue(false);
        }
        assertTrue(true);
    }
    
    public void testCrossValidate() {
        try {
            
            initializeDataSets();
            initializeEvaluators();
            initializeMLAlgorithms();
            for (TaskAlgorithm tAlgorithm : mlAlgorithms) {
                Table<String, String, Map<EvaluatorType, Double>> results = evaluator.crossValidate(tAlgorithm.getMlAlgorithm(), tasks,folds, evaluators, null);

            }
           
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertTrue(false);
        }
        assertTrue(true);
    }

    private void initializeEvaluators() {
        try {
            evaluators.add(EvaluatorType.PRECISION);
            evaluators.add(EvaluatorType.RECALL);
            evaluators.add(EvaluatorType.F_MEASURE);
            evaluators.add(EvaluatorType.P_PRECISION);
            evaluators.add(EvaluatorType.P_RECALL);
            evaluators.add(EvaluatorType.PF_MEASURE);
            evaluators.add(EvaluatorType.ACCURACY);
            //  DataSetChooser.getData("DRUGS");
        } catch (Exception e) {
            assertTrue(false);
        }
        assertTrue(true);
    }
    
    private List<LearningParameter> initializeLearningParameters(MLImplementationType mlType, String className) {
        List<LearningParameter> lParameters = null;
        if(mlType.equals(MLImplementationType.UNSUPERVISED))
            {
                if(className.equals("WOMBATSIMPLE"))
                    lParameters = initializeWombatSimple();
                
            }
        else  if(mlType.equals(MLImplementationType.SUPERVISED_ACTIVE))
            {
                if(className.equals("WOMBATSIMPLE"))
                    lParameters = initializeWombatSimple();

            }
        else  if(mlType.equals(MLImplementationType.SUPERVISED_BATCH))
            {
                if(className.equals("WOMBATSIMPLE"))
                    lParameters = initializeWombatSimple();

            }
        return lParameters;

    }
    
    private List<LearningParameter> initializeWombatSimple()
    {
        List<LearningParameter> wombaParameters = new ArrayList<>();
        
        wombaParameters.add(new LearningParameter(PARAMETER_MAX_REFINEMENT_TREE_SIZE, 2000, Integer.class, 10d, Integer.MAX_VALUE, 10d, PARAMETER_MAX_REFINEMENT_TREE_SIZE));
        wombaParameters.add(new LearningParameter(PARAMETER_MAX_ITERATIONS_NUMBER, 3, Integer.class, 1d, Integer.MAX_VALUE, 10d, PARAMETER_MAX_ITERATIONS_NUMBER));
        wombaParameters.add(new LearningParameter(PARAMETER_MAX_ITERATION_TIME_IN_MINUTES, 20, Integer.class, 1d, Integer.MAX_VALUE,1, PARAMETER_MAX_ITERATION_TIME_IN_MINUTES));
        wombaParameters.add(new LearningParameter(PARAMETER_EXECUTION_TIME_IN_MINUTES, 600, Integer.class, 1d, Integer.MAX_VALUE,1,PARAMETER_EXECUTION_TIME_IN_MINUTES));
        wombaParameters.add(new LearningParameter(PARAMETER_MAX_FITNESS_THRESHOLD, 1, Double.class, 0d, 1d, 0.01d, PARAMETER_MAX_FITNESS_THRESHOLD));
        wombaParameters.add(new LearningParameter(PARAMETER_MIN_PROPERTY_COVERAGE, 0.4, Double.class, 0d, 1d, 0.01d, PARAMETER_MIN_PROPERTY_COVERAGE));
        wombaParameters.add(new LearningParameter(PARAMETER_PROPERTY_LEARNING_RATE, 0.9,Double.class, 0d, 1d, 0.01d, PARAMETER_PROPERTY_LEARNING_RATE));
        wombaParameters.add(new LearningParameter(PARAMETER_OVERALL_PENALTY_WEIT, 0.5, Double.class, 0d, 1d, 0.01d, PARAMETER_OVERALL_PENALTY_WEIT));
        wombaParameters.add(new LearningParameter(PARAMETER_CHILDREN_PENALTY_WEIT, 1, Double.class, 0d, 1d, 0.01d, PARAMETER_CHILDREN_PENALTY_WEIT));
        wombaParameters.add(new LearningParameter(PARAMETER_COMPLEXITY_PENALTY_WEIT, 1, Double.class, 0d, 1d, 0.01d, PARAMETER_COMPLEXITY_PENALTY_WEIT));
        wombaParameters.add(new LearningParameter(PARAMETER_VERBOSE, false, Boolean.class, 0, 1, 0, PARAMETER_VERBOSE));
        wombaParameters.add(new LearningParameter(PARAMETER_MEASURES, new HashSet<String>(Arrays.asList("jaccard", "trigrams", "cosine", "qgrams")), MeasureType.class, 0, 0, 0, PARAMETER_MEASURES));
        wombaParameters.add(new LearningParameter(PARAMETER_SAVE_MAPPING, true, Boolean.class, 0, 1, 0, PARAMETER_SAVE_MAPPING));           
        return wombaParameters;   
        
    }
    //remember
    //---------AMLAlgorithm(concrete:SupervisedMLAlgorithm,ActiveMLAlgorithm or UnsupervisedMLAlgorithm--------
    //---                                                                                              --------
    //---         ACoreMLAlgorithm (concrete: EAGLE,WOMBAT,LION) u can retrieve it by get()            --------
    //---                                                                                              --------
    //---------------------------------------------------------------------------------------------------------
    
    private void initializeMLAlgorithms() {
        try
        {
            for (String alg : algorithmsList) {
                String[] algorithmInfo = alg.split(":");// split to get the type and the name of the algorithm
                MLImplementationType algType = MLImplementationType.valueOf(algorithmInfo[0]);// get the type of the algorithm
                //TODO implement initializeLearningParameters()
                List<LearningParameter> mlParameter = null;
                AMLAlgorithm algorithm = null;
                if(algType.equals(MLImplementationType.SUPERVISED_ACTIVE))
                {
                    if(algorithmInfo[0].equals("EAGLE"))//create its core as eagle - it will be enclosed inside SupervisedMLAlgorithm that extends AMLAlgorithm
                        algorithm =  MLAlgorithmFactory.createMLAlgorithm(Eagle.class,algType).asActive(); //create an eagle learning algorithm
                    else if(algorithmInfo[1].equals("WOMBATSIMPLE"))
                    {
                        algorithm =  MLAlgorithmFactory.createMLAlgorithm(WombatSimple.class,algType).asActive(); //create an eagle learning algorithm
                        WombatSimple ws = (WombatSimple)algorithm.getMl();
                        ws.setDefaultParameters();
                    }
                   mlParameter = initializeLearningParameters(MLImplementationType.SUPERVISED_ACTIVE,algorithmInfo[1]);

                }
                else if(algType.equals(MLImplementationType.SUPERVISED_BATCH))
                {
                    if(algorithmInfo[0].equals("EAGLE"))
                        algorithm =  MLAlgorithmFactory.createMLAlgorithm(Eagle.class,algType).asSupervised(); //create an eagle learning algorithm
                    else if(algorithmInfo[1].equals("WOMBATSIMPLE"))
                    {
                        algorithm =  MLAlgorithmFactory.createMLAlgorithm(WombatSimple.class,algType).asSupervised(); //create an eagle learning algorithm
/*                        WombatSimple ws = (WombatSimple)algorithm.getMl();
                        ws.setDefaultParameters();*/
                    }
                    mlParameter = initializeLearningParameters(MLImplementationType.SUPERVISED_BATCH,algorithmInfo[1]);

                }
                else if(algType.equals(MLImplementationType.UNSUPERVISED))
                {
                    if(algorithmInfo[0].equals("EAGLE"))
                        algorithm =  MLAlgorithmFactory.createMLAlgorithm(Eagle.class,algType).asUnsupervised(); //create an eagle learning algorithm
                    else if(algorithmInfo[1].equals("WOMBATSIMPLE"))
                    {
                        algorithm =  MLAlgorithmFactory.createMLAlgorithm(WombatSimple.class,algType).asUnsupervised(); //create an eagle learning algorithm
/*                        WombatSimple ws = (WombatSimple)algorithm.getMl();
                        ws.setDefaultParameters();*/
                    }
                    mlParameter = initializeLearningParameters(MLImplementationType.UNSUPERVISED,algorithmInfo[1]);

                }
                
                //TODO add other classes cases
                
                mlAlgorithms.add(new TaskAlgorithm(algType, algorithm, mlParameter));// add to list of algorithms
            }

        }catch (UnsupportedMLImplementationException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        assertTrue(true);
    }

    private void initializeDataSets() {
        TaskData task = new TaskData();
        DataSetChooser dataSets = new DataSetChooser();
        try {
            for (String ds : datasetsList) {
                System.out.println(ds);
                EvaluationData c = DataSetChooser.getData(ds);
                GoldStandard gs = new GoldStandard(c.getReferenceMapping());
                task = new TaskData(gs, c.getSourceCache(), c.getTargetCache());
                task.dataName = ds;

                //TODO assign the training data and the pseudoFM
                tasks.add(task);
            }


            //	DataSetChooser.getData("DRUGS");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertTrue(false);
        }
        assertTrue(true);
    }


}
