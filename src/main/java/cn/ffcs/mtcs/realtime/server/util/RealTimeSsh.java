package cn.ffcs.mtcs.realtime.server.util;

import cn.ffcs.common.basic.dao.JschShellDao;
import cn.ffcs.common.basic.security.SecurityFactory;
import cn.ffcs.mtcs.common.response.RetMsg;
import cn.ffcs.mtcs.realtime.common.entity.RtExeMachine;
import cn.ffcs.mtcs.realtime.server.exception.DataOpsException;
import cn.ffcs.mtcs.realtime.server.exception.SshExecuteException;
import cn.ffcs.mtcs.ssh.common.request.ExeInfo;
import cn.ffcs.mtcs.ssh.common.request.ExecuteMachine;
import cn.ffcs.mtcs.ssh.common.request.SshParamRequest;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Random;

@Slf4j
public class RealTimeSsh {


    private static Integer SSH_TIME_OUT = 60000;

    private static String DEFAULT_CRYPT_KEY = "k38d81C!@#dkro22232JAMDGIJGDSe48dk>KUY%%$";

    private static ExeInfo getExeInfo(List<ExeInfo> exeInfoList, int seek, int i) {
        int index = seek + i;
        int listSize = exeInfoList.size();
        if (listSize == 1) {
            return exeInfoList.get(0);
        }
        if (index >= listSize) {
            return exeInfoList.get(index % listSize);
        } else {
            return exeInfoList.get(index);
        }
    }

    private static void checkExecuteMachine(ExecuteMachine executeMachine) {
        Assert.notNull(executeMachine.getIp().trim(), "执行机IP为空！");
        Assert.notNull(executeMachine.getPort().trim(), "执行机Port为空");
        Assert.notNull(executeMachine.getUser().trim(), "执行机用户为空");
        Assert.notNull(executeMachine.getPassword().trim(), "执行机密码为空");
    }


    public static JSONObject executeBase(SshParamRequest paramRequest, int exeCountDefault) throws DataOpsException {

        JSONObject jsonObject = null;
        List<ExeInfo> exeInfoList = paramRequest.getExeInfoList();
        if (exeInfoList == null || exeInfoList.size() == 0) {
            throw new DataOpsException("执行信息为空！");
        }

        Random random = new Random();
        //正常不应该有多个执行机
        int seek = random.nextInt(exeInfoList.size());
        for (int i = 0; i < exeCountDefault; i++) {

            ExeInfo exeInfo = getExeInfo(exeInfoList, seek, i);
            String cmd = exeInfo.getExeCommand();
            ExecuteMachine executeMachine = exeInfo.getExecuteMachine();
            checkExecuteMachine(executeMachine);

            //获取密码
            String password = exeInfo.getExecuteMachine().getPassword();
            //解密
            String decodePassword = SecurityFactory.decode(SecurityFactory.DesRandom, password, DEFAULT_CRYPT_KEY);

            JschShellDao jschShellDao = null;
            try {
                jschShellDao = new JschShellDao();

                jschShellDao.connect(exeInfo.getExecuteMachine().getIp(), Integer.parseInt(exeInfo.getExecuteMachine().getPort()),
                        exeInfo.getExecuteMachine().getUser(), decodePassword, SSH_TIME_OUT);

                log.debug("cmd={}", cmd);
                jsonObject = jschShellDao.excute(cmd);
                String stdMsg = (String) jsonObject.get("stdMsg");
                String errMsg = (String) jsonObject.get("errMsg");
                int resCode = (int) jsonObject.get("resCode");
                if (resCode == 0 || resCode == 1) {
                    log.debug("stdMsg:{}", stdMsg);
                    return jsonObject;
                } else {
                    log.error("执行过程中失败！\n {}", errMsg);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());

            } finally {
                if (jschShellDao != null) {
                    jschShellDao.close();
                }
            }
        }
        return jsonObject;
    }

    public static RetMsg executeReturnMsg(SshParamRequest paramRequest, int exeCountDefault)
            throws DataOpsException {
        RetMsg retMsg = new RetMsg();
        JSONObject jsonObject = executeBase(paramRequest, exeCountDefault);
        if (jsonObject != null) {
            int resCode = (int) jsonObject.get("resCode");
            if (resCode == 0 || resCode == 1) {
                retMsg.setSuccess(true);
                retMsg.setMsg((String) jsonObject.get("stdMsg"));
            } else {
                retMsg.setSuccess(false);
                retMsg.setMsg((String) jsonObject.get("errMsg"));
            }
        } else {
            retMsg.setSuccess(false);
        }
        return retMsg;

    }

    public static RetMsg sshExeCmd(List<ExeInfo> exeInfoList, String cmd, int exeCountDefault) throws DataOpsException {
        RetMsg retMsg = new RetMsg();
        if (exeInfoList == null || exeInfoList.size() == 0) {
            throw new DataOpsException("执行信息为空！");
        }

        Random random = new Random();
        //正常不应该有多个执行机
        int seek = random.nextInt(exeInfoList.size());
        for (int i = 0; i < exeCountDefault; i++) {

            ExeInfo exeInfo = getExeInfo(exeInfoList, seek, i);
            ExecuteMachine executeMachine = exeInfo.getExecuteMachine();
            checkExecuteMachine(executeMachine);
            JschShellDao jschShellDao = null;
            try {
                jschShellDao = new JschShellDao();
                //获取密码
                String password = exeInfo.getExecuteMachine().getPassword();
                //解密
                String decodePassword = SecurityFactory.decode(SecurityFactory.DesRandom, password, DEFAULT_CRYPT_KEY);
                jschShellDao.connect(exeInfo.getExecuteMachine().getIp(), Integer.parseInt(exeInfo.getExecuteMachine().getPort()),
                        exeInfo.getExecuteMachine().getUser(), decodePassword, SSH_TIME_OUT);
                log.debug("cmd={}", cmd);
                jschShellDao.excute(cmd);
                JSONObject jsonObject = jschShellDao.excute(cmd);
                int resCode = (int) jsonObject.get("resCode");
                if (resCode == 0 || resCode == 1) {
                    retMsg.setSuccess(true);
                    retMsg.setMsg((String) jsonObject.get("stdMsg"));
                    break;
                } else {
                    retMsg.setSuccess(false);
                    retMsg.setMsg((String) jsonObject.get("errMsg"));
                }


            } catch (Exception e) {
                retMsg.setSuccess(false);
                retMsg.setMsg("执行异常");
            } finally {
                if (jschShellDao != null) {
                    jschShellDao.close();
                }
            }

        }
        return retMsg;

    }

    public static Boolean execute(SshParamRequest paramRequest, int exeCountDefault) throws DataOpsException {

        JSONObject jsonObject = executeBase(paramRequest, exeCountDefault);

        return (jsonObject != null) && ((int) jsonObject.get("resCode") == 0
                || (int) jsonObject.get("resCode") == 1);
    }

    public static JSONObject executeParam(SshParamRequest paramRequest, int exeCountDefault) throws DataOpsException {

        List<ExeInfo> exeInfoList = paramRequest.getExeInfoList();
        if (exeInfoList == null || exeInfoList.size() == 0) {
            throw new DataOpsException("执行信息为空！");
        }

        Random random = new Random();
        JSONObject jsonObject = null;
        int seek = random.nextInt(exeInfoList.size());
        for (int i = 0; i < exeCountDefault; i++) {

            ExeInfo exeInfo = getExeInfo(exeInfoList, seek, i);
            String cmd = exeInfo.getExeCommand();
            ExecuteMachine executeMachine = exeInfo.getExecuteMachine();
            checkExecuteMachine(executeMachine);

            //获取密码
            String password = exeInfo.getExecuteMachine().getPassword();
            //解密
            String decodePassword = SecurityFactory.decode(SecurityFactory.DesRandom, password, DEFAULT_CRYPT_KEY);
            JschShellDao jschShellDao = null;
            try {
                jschShellDao = new JschShellDao();
                jschShellDao.connect(exeInfo.getExecuteMachine().getIp(), Integer.parseInt(exeInfo.getExecuteMachine().getPort()),
                        exeInfo.getExecuteMachine().getUser(), decodePassword, SSH_TIME_OUT);

                log.info("cmd={}", cmd);
                jsonObject = jschShellDao.excute(cmd);

                int resCode = (int) jsonObject.get("resCode");


                if (resCode == 0 || resCode == 1) {
                    break;
                } else {
                    String errMsg = (String) jsonObject.get("errMsg");
                    log.error("执行过程中失败！\n {}", errMsg);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            } finally {
                if (jschShellDao != null) {
                    jschShellDao.close();
                }
            }
        }
        ;
        return jsonObject;
    }


    public static void main(String[] args) {
        String password = args[0];
        log.info("password:{}", password);
        String securityPassword = SecurityFactory.encode(SecurityFactory.DesRandom, password, DEFAULT_CRYPT_KEY);
        log.info("securityPassword:{}", securityPassword);
        String decodePassword = SecurityFactory.decode(SecurityFactory.DesRandom, securityPassword, DEFAULT_CRYPT_KEY);
        log.info("decodePassword:{}", decodePassword);

    }
}
