import { TgMasterWithChartBehavior} from '/resources/master/tg-master-with-chart-behavior.js';

const TgScatterPlotMasterBehaviorImpl = {

    ready: function () {
        TgMasterWithChartBehavior.ready();
    },

    

};

export const TgScatterPlotMasterBehavior = [
    TgMasterWithChartBehavior,
    TgScatterPlotMasterBehaviorImpl
];