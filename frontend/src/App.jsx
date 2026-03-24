import React, { useEffect, useMemo, useState } from 'react'
import {
  AppBar, Toolbar, Typography, Container, Grid, Paper, TextField, Button,
  IconButton, Divider, Alert, Snackbar, Box, Chip, MenuItem, Select, InputLabel, FormControl,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import SearchIcon from '@mui/icons-material/Search'
import SyncAltIcon from '@mui/icons-material/SyncAlt'
import RefreshIcon from '@mui/icons-material/Refresh'
import ContentCopyIcon from '@mui/icons-material/ContentCopy'
import { OrdersApi } from './api.js'

const STATUSES = ['CREATED', 'PAID', 'COOKING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];
const SIZES = ['SMALL', 'MEDIUM', 'LARGE'];

function emptyItem() {
  return { name: '', size: 'MEDIUM', qty: 1 };
}

export default function App() {
  // --- Create order form state ---
  const [address, setAddress] = useState({
    city: 'Amsterdam',
    street: 'Damrak',
    house: '1',
    apartment: '12',
    postcode: '1012'
  });
  const [items, setItems] = useState([emptyItem()]);
  const [creating, setCreating] = useState(false);

  // --- Orders table state ---
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState('');

  // --- Right panel: get/change status ---
  const [orderId, setOrderId] = useState('');
  const [loadingOrder, setLoadingOrder] = useState(false);
  const [order, setOrder] = useState(null);

  const [statusToSet, setStatusToSet] = useState('PAID');
  const [changing, setChanging] = useState(false);

  const [toast, setToast] = useState({ open: false, severity: 'success', message: '' });

  const canCreate = useMemo(() => {
    if (!address.city || !address.street || !address.house || !address.postcode) return false;
    if (!items.length) return false;
    return items.every(i => i.name && i.size && Number(i.qty) > 0);
  }, [address, items]);

  const showError = (e) => {
    const msg = e?.message || 'Ошибка';
    setToast({ open: true, severity: 'error', message: msg });
  };

  const loadOrders = async () => {
    setLoadingOrders(true);
    try {
      const list = await OrdersApi.listOrders();
      setOrders(Array.isArray(list) ? list : []);
    } catch (e) {
      showError(e);
      setOrders([]);
    } finally {
      setLoadingOrders(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const onCreate = async () => {
    setCreating(true);
    try {
      const payload = { address, items: items.map(i => ({ ...i, qty: Number(i.qty) })) };
      const created = await OrdersApi.createOrder(payload);
      setOrder(created);
      setOrderId(created.id);
      setSelectedOrderId(created.id);
      setToast({ open: true, severity: 'success', message: 'Заказ создан: ' + created.id });
      await loadOrders();
    } catch (e) {
      showError(e);
    } finally {
      setCreating(false);
    }
  };

  const onGet = async (id = orderId) => {
    const targetId = id?.trim();
    if (!targetId) return;
    setLoadingOrder(true);
    try {
      const got = await OrdersApi.getOrder(targetId);
      setOrder(got);
      setOrderId(targetId);
      setSelectedOrderId(targetId);
      setToast({ open: true, severity: 'success', message: 'Заказ загружен' });
    } catch (e) {
      showError(e);
      setOrder(null);
    } finally {
      setLoadingOrder(false);
    }
  };

  const onChangeStatus = async () => {
    if (!order?.id) return;
    setChanging(true);
    try {
      const updated = await OrdersApi.changeStatus(order.id, statusToSet);
      setOrder(updated);
      setToast({ open: true, severity: 'success', message: 'Статус обновлён: ' + updated.status });
      await loadOrders();
    } catch (e) {
      showError(e);
    } finally {
      setChanging(false);
    }
  };

  const onSelectRow = async (id) => {
    setSelectedOrderId(id);
    setOrderId(id);
    await onGet(id);
  };

  const copyToClipboard = async (text) => {
    try {
      await navigator.clipboard.writeText(text);
      setToast({ open: true, severity: 'success', message: 'Скопировано: ' + text });
    } catch {
      setToast({ open: true, severity: 'info', message: 'Не удалось скопировать автоматически — выделите и скопируйте вручную.' });
    }
  };

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Pizza Delivery — учебный UI (React + MUI)
          </Typography>
          <Chip label="REST: /orders" color="default" variant="outlined" />
        </Toolbar>
      </AppBar>

      <Container sx={{ mt: 3, mb: 6 }}>
        <Grid container spacing={2}>
          {/* Left column: Orders table + Create */}
          <Grid item xs={12} md={7}>
            <Paper sx={{ p: 2, mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Box>
                  <Typography variant="h6">Orders</Typography>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                    Таблица для выбора заказа. Клик по строке заполнит форму справа.
                  </Typography>
                </Box>
                <Box>
                  <Tooltip title="Refresh">
                    <span>
                      <IconButton onClick={loadOrders} disabled={loadingOrders}>
                        <RefreshIcon />
                      </IconButton>
                    </span>
                  </Tooltip>
                </Box>
              </Box>

              <Divider sx={{ my: 2 }} />

              {orders.length === 0 ? (
                <Alert severity="info">
                  {loadingOrders ? 'Загружаю список...' : 'Пока нет заказов. Создайте первый заказ ниже.'}
                </Alert>
              ) : (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>ID</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell align="right">Total</TableCell>
                        <TableCell>Zone</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {orders.map((o) => (
                        <TableRow
                          key={o.id}
                          hover
                          selected={o.id === selectedOrderId}
                          onClick={() => onSelectRow(o.id)}
                          sx={{ cursor: 'pointer' }}
                        >
                          <TableCell>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Box sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                                {String(o.id).slice(0, 8)}…
                              </Box>
                              <Tooltip title="Copy full UUID">
                                <IconButton size="small" onClick={(e) => { e.stopPropagation(); copyToClipboard(o.id); }}>
                                  <ContentCopyIcon fontSize="inherit" />
                                </IconButton>
                              </Tooltip>
                            </Box>
                          </TableCell>
                          <TableCell>
                            <Chip size="small" label={o.status} />
                          </TableCell>
                          <TableCell align="right">
                            {o.totalAmount} {o.currency}
                          </TableCell>
                          <TableCell>{o.deliveryZone}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Paper>

            <Paper sx={{ p: 2 }}>
              <Typography variant="h6">Create Order</Typography>
              <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
                Заполняем адрес и позиции. Нажимаем Create — уходим в use case CreateOrder.
              </Typography>

              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>Address</Typography>
                <Grid container spacing={1}>
                  <Grid item xs={12} sm={6}>
                    <TextField fullWidth label="City" value={address.city}
                      onChange={(e) => setAddress({ ...address, city: e.target.value })} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField fullWidth label="Postcode" value={address.postcode}
                      onChange={(e) => setAddress({ ...address, postcode: e.target.value })} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField fullWidth label="Street" value={address.street}
                      onChange={(e) => setAddress({ ...address, street: e.target.value })} />
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <TextField fullWidth label="House" value={address.house}
                      onChange={(e) => setAddress({ ...address, house: e.target.value })} />
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <TextField fullWidth label="Apartment" value={address.apartment}
                      onChange={(e) => setAddress({ ...address, apartment: e.target.value })} />
                  </Grid>
                </Grid>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Box>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Typography variant="subtitle2">Items</Typography>
                  <Button startIcon={<AddIcon />} onClick={() => setItems([...items, emptyItem()])}>
                    Add item
                  </Button>
                </Box>

                {items.map((it, idx) => (
                  <Grid container spacing={1} sx={{ mt: 0.5 }} key={idx}>
                    <Grid item xs={12} sm={5}>
                      <TextField fullWidth label="Name" value={it.name}
                        onChange={(e) => {
                          const next = [...items]; next[idx] = { ...it, name: e.target.value }; setItems(next);
                        }} />
                    </Grid>
                    <Grid item xs={6} sm={3}>
                      <TextField
                        select
                        fullWidth
                        label="Size"
                        value={it.size}
                        onChange={(e) => {
                          const next = [...items]; next[idx] = { ...it, size: e.target.value }; setItems(next);
                        }}
                      >
                        {SIZES.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                      </TextField>
                    </Grid>
                    <Grid item xs={4} sm={2}>
                      <TextField fullWidth label="Qty" type="number" inputProps={{ min: 1 }}
                        value={it.qty}
                        onChange={(e) => {
                          const next = [...items]; next[idx] = { ...it, qty: e.target.value }; setItems(next);
                        }} />
                    </Grid>
                    <Grid item xs={2} sm={2} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
                      <IconButton
                        aria-label="delete"
                        disabled={items.length === 1}
                        onClick={() => setItems(items.filter((_, i) => i !== idx))}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Grid>
                  </Grid>
                ))}
              </Box>

              <Divider sx={{ my: 2 }} />

              <Button
                variant="contained"
                onClick={onCreate}
                disabled={!canCreate || creating}
                fullWidth
              >
                {creating ? 'Creating...' : 'Create Order'}
              </Button>

              {!canCreate && (
                <Alert severity="info" sx={{ mt: 2 }}>
                  Заполните адрес и хотя бы одну позицию (name/size/qty).
                </Alert>
              )}
            </Paper>
          </Grid>

          {/* Right column: details + status */}
          <Grid item xs={12} md={5}>
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6">Order details</Typography>
              <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
                Можно выбрать заказ слева, либо вставить UUID вручную.
              </Typography>

              <Grid container spacing={1} alignItems="center">
                <Grid item xs={12} sm={9}>
                  <TextField
                    fullWidth
                    label="Order ID"
                    value={orderId}
                    onChange={(e) => setOrderId(e.target.value)}
                    placeholder="UUID..."
                  />
                </Grid>
                <Grid item xs={12} sm={3}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<SearchIcon />}
                    onClick={() => onGet()}
                    disabled={!orderId || loadingOrder}
                  >
                    {loadingOrder ? '...' : 'Get'}
                  </Button>
                </Grid>
              </Grid>

              <Divider sx={{ my: 2 }} />

              <Grid container spacing={1} alignItems="center">
                <Grid item xs={12} sm={7}>
                  <FormControl fullWidth>
                    <InputLabel id="status-label">New status</InputLabel>
                    <Select
                      labelId="status-label"
                      label="New status"
                      value={statusToSet}
                      onChange={(e) => setStatusToSet(e.target.value)}
                    >
                      {STATUSES.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={5}>
                  <Button
                    fullWidth
                    variant="contained"
                    startIcon={<SyncAltIcon />}
                    onClick={onChangeStatus}
                    disabled={!order?.id || changing}
                  >
                    {changing ? '...' : 'Change'}
                  </Button>
                </Grid>
              </Grid>

              <Divider sx={{ my: 2 }} />

              <Typography variant="subtitle2" sx={{ mb: 1 }}>Current order (JSON)</Typography>
              {order ? (
                <Box sx={{ fontFamily: 'monospace', fontSize: 13, whiteSpace: 'pre-wrap' }}>
                  {JSON.stringify(order, null, 2)}
                </Box>
              ) : (
                <Alert severity="warning">
                  Заказ не загружен. Выберите строку слева или выполните Get по id.
                </Alert>
              )}

              <Alert severity="info" sx={{ mt: 2 }}>
                Подсказка: попробуйте нарушение бизнес правила статуса (например, DELIVERED → PAID) — получите 409.
              </Alert>
            </Paper>
          </Grid>
        </Grid>
      </Container>

      <Snackbar
        open={toast.open}
        autoHideDuration={4000}
        onClose={() => setToast({ ...toast, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={() => setToast({ ...toast, open: false })} severity={toast.severity} sx={{ width: '100%' }}>
          {toast.message}
        </Alert>
      </Snackbar>
    </>
  )
}
